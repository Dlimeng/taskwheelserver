package com.task.util

import java.io.IOException
import java.util
import java.util.Properties

import org.slf4j.{Logger, LoggerFactory}

/**
  * @author limeng
  * @date 2019/5/30 23:53
  * @version 1.0
  */
object PropertiesUtils {
  final  val defaultPath="/connection/connections.properties"
  private val propertiesMap = new util.HashMap[String, Properties]()
  private val logger: Logger = LoggerFactory.getLogger(PropertiesUtils.getClass)
  def apply(path:String):Properties={
    this.properties(path)
  }

  def apply():Properties={
    this.properties(defaultPath)
  }
  def properties(path: String): Properties ={
    var properties=propertiesMap.get(path)
    if(null == properties) {
      try{
        properties = new Properties
        properties.load(getClass.getResourceAsStream(path))
        propertiesMap.put(path, properties)
      }catch {
        case  e:IOException =>logger.error(e.getMessage)
      }
    }
    properties
  }
}
