/*
 * Copyright 2009-2015 DigitalGlobe, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.mrgeo.spark

import java.awt.image.{DataBuffer, WritableRaster}
import java.io.{Externalizable, ObjectInput, ObjectOutput}

import org.apache.hadoop.conf.Configuration
import org.apache.spark.rdd.{CoGroupedRDD, RDD}
import org.apache.spark.{HashPartitioner, SparkConf, SparkContext}
import org.mrgeo.data.{ProviderProperties, DataProviderFactory}
import org.mrgeo.data.DataProviderFactory.AccessMode
import org.mrgeo.data.raster.RasterWritable
import org.mrgeo.data.tile.TileIdWritable
import org.mrgeo.spark.job.{JobArguments, MrGeoDriver, MrGeoJob}
import org.mrgeo.utils.TMSUtils.TileBounds
import org.mrgeo.utils._

import scala.collection.mutable
import scala.reflect.ClassTag
import scala.util.control._

object MosaicDriver extends MrGeoDriver with Externalizable {

  def mosaic(inputs: Array[String], output:String, conf:Configuration): Unit = {

    val args =  mutable.Map[String, String]()

    val in = inputs.mkString(",")
    val name = "Mosaic (" + in + ")"

    args += "inputs" -> in
    args += "output" -> output


    run(name, classOf[MosaicDriver].getName, args.toMap, conf)
  }

  override def writeExternal(out: ObjectOutput): Unit = {}
  override def readExternal(in: ObjectInput): Unit = {}

  override def setup(job: JobArguments): Boolean = {
    true
  }
}

class MosaicDriver extends MrGeoJob with Externalizable {
  var inputs: Array[String] = null
  var output:String = null

  override def registerClasses(): Array[Class[_]] = {
    val classes = Array.newBuilder[Class[_]]

    // yuck!  need to register spark private classes
    classes += ClassTag(Class.forName("org.apache.spark.util.collection.CompactBuffer")).wrap.runtimeClass

    classes.result()
  }

  override def execute(context: SparkContext): Boolean = {

    implicit val tileIdOrdering = new Ordering[TileIdWritable] {
      override def compare(x: TileIdWritable, y: TileIdWritable): Int = x.compareTo(y)
    }

    logInfo("MosaicDriver.execute")

    val pyramids = Array.ofDim[RDD[(TileIdWritable, RasterWritable)]](inputs.length)
    val nodata = Array.ofDim[Array[Double]](inputs.length)

    var i:Int = 0
    var zoom:Int = -1
    var tilesize:Int = -1
    var tiletype:Int = -1
    var numbands:Int = -1
    val bounds:Bounds = new Bounds()

    // loop through the inputs and load the pyramid RDDs and metadata
    for (input <- inputs) {

      logInfo("Loading pyramid: " + input)
      try {
        val pyramid = SparkUtils.loadMrsPyramidAndMetadataRDD(input, context)
        pyramids(i) = pyramid._1

        nodata(i) = pyramid._2.getDefaultValues

        // check for the same max zooms
        if (zoom < 0) {
          zoom = pyramid._2.getMaxZoomLevel
        }
        else if (zoom != pyramid._2.getMaxZoomLevel) {
          throw new IllegalArgumentException("All images must have the same max zoom level. " +
              pyramid._2.getPyramid + " is " + pyramid._2.getMaxZoomLevel + ", others are " + zoom)
        }

        if (tilesize < 0) {
          tilesize = pyramid._2.getTilesize
        }
        else if (tilesize != pyramid._2.getTilesize) {
          throw new IllegalArgumentException("All images must have the same tilesize. " +
              pyramid._2.getPyramid + " is " + pyramid._2.getTilesize + ", others are " + tilesize)
        }

        if (tiletype < 0) {
          tiletype = pyramid._2.getTileType
        }
        else if (tiletype != pyramid._2.getTileType) {
          throw new IllegalArgumentException("All images must have the same tile type. " +
              pyramid._2.getPyramid + " is " + pyramid._2.getTileType + ", others are " + tiletype)
        }

        if (numbands < 0) {
          numbands = pyramid._2.getBands
        }
        else if (numbands != pyramid._2.getBands) {
          throw new IllegalArgumentException("All images must have the same number of bands. " +
              pyramid._2.getPyramid + " is " + pyramid._2.getBands + ", others are " + numbands)
        }

        // expand the total bounds
        bounds.expand(pyramid._2.getBounds)
      }
      catch {
        case e:Exception =>   logError("ERROR Loading pyramid: " + input, e)

      }

      i += 1
    }

    val tileBounds:TileBounds = TMSUtils.boundsToTile(bounds.getTMSBounds, zoom, tilesize)

    logDebug("Bounds: " + bounds.toString)
    logDebug("TileBounds: " + tileBounds.toString)

    // cogroup needs a partitioner, so we'll give one here...
    var maxpartitions = 0
    val partitions = pyramids.foreach(p => {
      if (p.partitions.length > maxpartitions) {
        maxpartitions = p.partitions.length
      }
    })
    val groups = new CoGroupedRDD(pyramids, new HashPartitioner(maxpartitions))

    val mosaiced:RDD[(TileIdWritable, RasterWritable)] = groups.map(U => {

      def isnodata(sample:Double, nodata:Double): Boolean = {
        if (nodata.isNaN) {
          if (sample.isNaN) return true
        }
        else if (nodata == sample) return true
        false
      }

      var dst: WritableRaster = null
      var dstnodata:Array[Double] = null

      val done = new Breaks
      var img:Int = 0
      done.breakable {
        for (wr<- U._2) {
          if (wr != null && wr.nonEmpty) {
            val writable = wr.asInstanceOf[Seq[RasterWritable]].head

            if (dst == null) {
              // the tile conversion is a WritableRaster, we can just typecast here
              dst = RasterWritable.toRaster(writable).asInstanceOf[WritableRaster]
              dstnodata = nodata(img)

              val looper = new Breaks

              // check if there are any nodatas in the 1st tile
              looper.breakable {
                for (y <- 0 until dst.getHeight) {
                  for (x <- 0 until dst.getWidth) {
                    for (b <- 0 until dst.getNumBands) {
                      if (isnodata(dst.getSampleDouble(x, y, b), dstnodata(b))) {
                        looper.break()
                      }
                    }
                  }
                }
                // we only get here if there aren't any nodatas, so we can just take the 1st tile verbatim
                done.break()
              }
            }
            else {
              // do the mosaic
              var hasnodata = false

              // the tile conversion is a WritableRaster, we can just typecast here
              val src = RasterWritable.toRaster(writable).asInstanceOf[WritableRaster]
              val srcnodata = nodata(img)

              for (y <- 0 until dst.getHeight) {
                for (x <- 0 until dst.getWidth) {
                  for (b <- 0 until dst.getNumBands) {
                    if (isnodata(dst.getSampleDouble(x, y, b), dstnodata(b))) {
                      val sample = src.getSampleDouble(x, y, b)
                      // if the src is also nodata, remember this, we still have to look in other tiles
                      if (isnodata(sample, srcnodata(b))) {
                        hasnodata = true
                      }
                      else {
                        dst.setSample(x, y, b, sample)
                      }
                    }
                  }
                }
              }
              // we've filled up the tile, nothing left to do...
              if (!hasnodata) {
                done.break()
              }
            }
          }
          img += 1
        }
      }

      // write the tile...
      (new TileIdWritable(U._1), RasterWritable.toWritable(dst))
    })


    val op = DataProviderFactory.getMrsImageDataProvider(output, AccessMode.WRITE, null.asInstanceOf[ProviderProperties])

    SparkUtils.saveMrsPyramidRDD(mosaiced, op, zoom, tilesize, Array[Double](Float.NaN),
      context.hadoopConfiguration, DataBuffer.TYPE_FLOAT, bounds, bands = 1,
      protectionlevel = null) // metadata.getProtectionLevel)

    true
  }

  override def setup(job: JobArguments, conf:SparkConf): Boolean = {

    val in:String = job.getSetting("inputs")

    inputs = in.split(",")
    output = job.getSetting("output")

    true
  }


  override def teardown(job: JobArguments, conf:SparkConf): Boolean = {
    true
  }

  override def writeExternal(out: ObjectOutput): Unit = {}

  override def readExternal(in: ObjectInput): Unit = {}
}
