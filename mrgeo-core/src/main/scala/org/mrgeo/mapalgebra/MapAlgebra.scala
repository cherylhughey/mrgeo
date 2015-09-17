package org.mrgeo.mapalgebra

import java.io.{Externalizable, ObjectInput, ObjectOutput}
import java.util.regex.Pattern

import akka.actor.FSM.->
import org.apache.hadoop.conf.Configuration
import org.apache.spark.{SparkConf, SparkContext}
import org.mrgeo.data
import org.mrgeo.data.DataProviderFactory.AccessMode
import org.mrgeo.data.{DataProviderFactory, ProviderProperties}
import org.mrgeo.mapalgebra.parser._
import org.mrgeo.mapalgebra.raster.{RasterMapOp, MrsPyramidMapOp}
import org.mrgeo.spark.job.{JobArguments, MrGeoDriver, MrGeoJob}
import org.mrgeo.utils.{HadoopUtils, StringUtils}

import scala.collection.JavaConversions._
import scala.collection.mutable

object MapAlgebra extends MrGeoDriver {

  final private val MapAlgebra = "mapalgebra"
  final private val Output = "output"
  final private val ProtectionLevel = "protection.level"
  final private val ProviderProperties = "provider.properties"

  def mapalgebra(expression:String, output:String,
      conf:Configuration, providerProperties: ProviderProperties, protectionLevel:String = null):Boolean = {
    val args = mutable.Map[String, String]()

    val name = "MapAlgebra"

    args += MapAlgebra -> expression
    args += Output -> output

    args += ProtectionLevel -> { if (protectionLevel == null) "" else protectionLevel }
    args += ProviderProperties -> { if (providerProperties == null) "" else data.ProviderProperties.toDelimitedString(providerProperties) }

    run(name, classOf[MapAlgebra].getName, args.toMap, conf)

    true
  }

  def validate(expression:String, providerProperties: ProviderProperties):Boolean =
    new MapAlgebra().isValid(expression, providerProperties)

  override def setup(job: JobArguments): Boolean = true
}

class MapAlgebra() extends MrGeoJob with Externalizable {
  private val filePattern = Pattern.compile("\\s*\\[([^\\]]+)\\]\\s*")
  private val parser = ParserAdapterFactory.createParserAdapter
  private val variables = mutable.Map.empty[ParserVariableNode, Option[ParserNode]]


  parser.initialize()


  def isValid(expression:String,
      providerproperties: ProviderProperties = ProviderProperties.fromDelimitedString("")):Boolean = {
    try {
      val nodes = parse(expression)
    }
    catch {
      // any exception means an error
      case p:ParserException =>
        logError("Parser error!  " + p.getMessage)
        return false
      case e:Exception => return false
    }

    true
  }

  private def parse(expression: String, protectionLevel:String = null): Array[ParserNode] = {
    val nodes = Array.newBuilder[ParserNode]

    val cleaned = cleanExpression(expression)

    val mapped = mapFiles(cleaned)

    val lines = mapped.split(";")

    lines.foreach(line => {
      val node = parser.parse(line)
      buildMapOps(node, protectionLevel)
      nodes += node
    })

    nodes.result()
  }

  private def cleanExpression(expression: String): String = {
    logInfo("Raw expression: " + expression)

    val lines = expression.split("\n")

    val cleanlines = lines.map(raw => {
      val line = raw.trim
      if (line.isEmpty || line.startsWith("#")) {
        ""
      }
      else if (line.indexOf("#") > 0) {
        line.substring(0, line.indexOf("#")).trim
      }
      else {
        line
      }
    })

    val cleanexp = StringUtils.join(cleanlines, ";")
    logInfo("Cleaned expression: " + cleanexp)
    cleanexp
  }

  // we have to map files differently because the parser doesn't know about the "[<file>]" syntax
  private def mapFiles(expression: String) = {

    var exp = expression
    var i: Int = 0

    var matcher = filePattern.matcher(exp)
    while (matcher.find) {
      val file: String = matcher.group(1)

      //if (!variables.contains(file)) {
      val variable = "__file_" + i + "__"

      val vn = new ParserVariableNode
      vn.setNativeNode(null)
      vn.setName(variable)

      val fn = new ParserFunctionNode
      fn.setName(file)
      fn.setMapOp(loadResource(file).orNull )

      variables.put(vn, Some(fn))
      exp = exp.replace("[" + file + "]", variable)
      i += 1
      //}
      matcher = filePattern.matcher(exp)
    }
    logInfo("Expression with files mapped: " + exp)

    exp
  }

  def findVariable(name: String): Option[ParserNode] = {
    variables.find(variable => variable._1.getName == name) match {
    case Some(v) => v._2
    case None => None
    }
  }

  private def buildMapOps(node: ParserNode, protectionLevel:String = null):Unit = {
    // special case for "="
    node match {
    case function: ParserFunctionNode =>
      val name = function.getName
      if (name == "=") {
        if (function.getNumChildren != 2) {
          throw new ParserException("Variable \"" + name +
              "\" must be in the form " + name + " =  <expression>")
        }

        val variable = function.getChild(0)

        variable match {
        case v: ParserVariableNode =>
          if (MapOpFactory.exists(variable.getName)) {
            throw new ParserException("Cannot use variable name \"" + name +
                "\" because there is a function of the same name")
          }
          val value = function.getChild(1)

          buildMapOps(value, protectionLevel)
          variables.put(v, Some(value))

        case _ => throw new ParserException("Left side of \"=\" must be a valid variable name")
        }

        return
      }

    case _ =>
    }

    node.getChildren.foreach(child => {
      buildMapOps(child)
    })

    node match {
    case const: ParserConstantNode =>

    case variable: ParserVariableNode =>
      val name = variable.getName
      if (findVariable(name).isEmpty) {
        throw new ParserException("Variable \"" + name +
            "\" must be defined before used")
      }
    case function: ParserFunctionNode =>
      val name = function.getName
      // Remember,  "=" was handled above
      if (!MapOpFactory.exists(name)) {
        throw new ParserException("Function \"" + name + "\" does not exist")
      }

      // NOTE:  mapop constructor should throw ParserExceptions on error
      MapOpFactory(function, findVariable, protectionLevel) match {
      case Some(op) => function.setMapOp(op)
      case _ =>
      }
    }
  }

  private def loadResource(name:String):Option[MapOp] = {
    val imdp = DataProviderFactory.getMrsImageDataProvider(name, AccessMode.READ, providerproperties)
    if (imdp != null) {
      return Some(MrsPyramidMapOp(imdp))
    }
    //TODO:  Implement Vector
    //    val vdp = DataProviderFactory.getVectorDataProvider(name, AccessMode.READ, conf)
    //    if (vdp != null) {
    //      return new VectorRDD(vdp)
    //    }

    None
  }

  override def registerClasses(): Array[Class[_]] = {
    val classes = Array.newBuilder[Class[_]]

    classes.result()
  }

  var expression:String = null
  var output:String = null
  var providerproperties: ProviderProperties = null
  var protectionLevel:String = null

  var nodes:Array[ParserNode] = null

  override def setup(job: JobArguments, conf: SparkConf): Boolean = {
    expression = job.getSetting(MapAlgebra.MapAlgebra)
    output = job.getSetting(MapAlgebra.Output)

    providerproperties = ProviderProperties.fromDelimitedString(
      job.getSetting(MapAlgebra.ProviderProperties))

    protectionLevel = job.getSetting(MapAlgebra.ProtectionLevel)
    if (protectionLevel.length == 0) {
      protectionLevel = null
    }

    nodes = parse(expression)

    nodes.foreach(node => {
      setup(node, job, conf)
    })

    true
  }

  private def setup(node:ParserNode, job: JobArguments, conf: SparkConf): Unit = {
    // depth first run
    node.getChildren.foreach(child => {
      setup(child, job, conf)
    })

    node match {
    case function:ParserFunctionNode =>
      function.getName match {
      case "=" =>  // ignore assignments...
      case _ =>
        val mapop = function.getMapOp

        if (mapop != null) {
          mapop.setup(job, conf)
        }
      }

    case _ => // no op, nothing to do if we're not a function (MapOp)
    }
  }

  override def execute(context: SparkContext): Boolean = {

    // we need to run through each variable and make sure the context is set.  Input files are
    // known to _not_ have the context set
    variables.values.foreach {
      case Some(variable) =>
        variable match {
        case function:ParserFunctionNode => function.getMapOp.context(context)
        case _ =>
        }
      case _ =>
    }

    // execute the mapalgebra
    nodes.foreach(node => {
      execute(node, context)
    })

    // now take the last RDD created and save it
    nodes.reverseIterator.foreach {
      case function: ParserFunctionNode =>
        function.getMapOp match {
        case rmo: RasterMapOp =>
          rmo.save(output, providerproperties, context)
          return true
        case _ =>
        }
      case _ =>
    }
    false
  }

  private def execute(node:ParserNode, context:SparkContext): Unit = {
    // depth first run
    node.getChildren.foreach(child => {
      execute(child, context)
    })

    node match {
    case function:ParserFunctionNode =>
      function.getName match {
      case "=" =>  // ignore assignments...
      case _ =>
        val mapop = function.getMapOp

        if (mapop != null) {
          mapop.execute(context)
        }
      }
    case _ => // no op, nothing to do if we're not a function (MapOp)
    }
  }

  override def teardown(job: JobArguments, conf: SparkConf): Boolean = true

  override def readExternal(in: ObjectInput): Unit = {}

  override def writeExternal(out: ObjectOutput): Unit = {}
}


object TestMapAlgebra extends App {

  val conf = HadoopUtils.createConfiguration()
  val pp = ProviderProperties.fromDelimitedString("")

  HadoopUtils.setupLocalRunner(conf)

  val expression = "x = 100; " +
      "y = [/mrgeo/images/small-elevation]; " +
      "z = 10 / [/mrgeo/images/small-elevation] + [/mrgeo/images/small-elevation] * [/mrgeo/images/small-elevation] - x + y; " +
      "a = [/mrgeo/images/small-elevation] + [/mrgeo/images/small-elevation] * [/mrgeo/images/small-elevation] + [/mrgeo/images/small-elevation]; " +
      "[/mrgeo/images/small-elevation] + [/mrgeo/images/small-elevation] * [/mrgeo/images/small-elevation] + [/mrgeo/images/small-elevation]"


  val output = "test-mapalgebra"
  if (MapAlgebra.validate(expression, pp)) {
    //MapAlgebra.mapalgebra(expression, output, conf, pp)
  }

  System.exit(0)
}
