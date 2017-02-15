/*
 * Copyright 2009-2017. DigitalGlobe, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.mrgeo.hdfs.partitioners

import java.io._

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.spark.Partitioner
import org.mrgeo.data.rdd.RasterRDD
import org.mrgeo.hdfs.image.HdfsMrsImageDataProvider
import org.mrgeo.hdfs.tile.FileSplit
import org.mrgeo.utils.SparkUtils

abstract class FileSplitPartitioner() extends Partitioner with Externalizable {
  def hasFixedPartitions:Boolean

  def calculateNumPartitions(raster:RasterRDD, output:String):Int = 1

  def writeSplits(rdd:RasterRDD, pyramid:String, zoom:Int, conf:Configuration) = {
    val fileSplits = new FileSplit

    val splitinfo = SparkUtils.calculateSplitData(rdd)
    fileSplits.generateSplits(splitinfo)

    val dp:HdfsMrsImageDataProvider = new HdfsMrsImageDataProvider(conf, pyramid, null)
    val inputWithZoom = new Path(dp.getResourcePath(false), "" + zoom)

    fileSplits.writeSplits(inputWithZoom)
  }

}
