package com.task.model

import java.util.concurrent.TimeUnit

import com.alibaba.fastjson.JSON
import com.typesafe.scalalogging.LazyLogging
import org.json4s.JsonAST.JValue
import org.json4s.{DefaultFormats, Formats, NoTypeHints}
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

import scala.reflect.{ClassTag, Manifest}




case class Test2(id:String,url:String)
/**
  * @author limeng
  * @date 2019/5/30 18:57
  * @version 1.0
  */
object TimerTaskList extends LazyLogging{
  def main(args: Array[String]): Unit = {
    val businessTask=BusinessTask("id1","url",Map("tets"->"ss"),"POST",null,22,33,false)
    val str:String ="""{"id":"ddddd","url":"ss"}"""
    val test:Test2=Test2("222","sss")

    //val obj: BusinessTask = JSON.parseObject(str, classOf[BusinessTask])
    //val str2 = JSON.toJSONString(businessTask)
    //println(str)

    import org.json4s.JsonDSL._
    import org.json4s.jackson.JsonMethods._
    import org.json4s.jackson.Serialization._
    import org.json4s.jackson.Serialization
    //implicit val formats = Serialization.formats(NoTypeHints)
    //val jsonstr = write(businessTask)
   // println(jsonstr)
    //implicit val formats = DefaultFormats
    //val str2: String = compact(render(businessTask.toString))
   // println(str2)
    //val sre22=getObjectFromString[Test2](str)
    //println(sre22)
  }


  def getObjectFromString[T:JValue](objBody:String)={
    implicit val formats = DefaultFormats
    parse(objBody).extract[T]
  }

}

/**
  * 任务
  * @param id 任务唯一id
  * @param url 请求地址
  * @param params 参数
  * @param requestMethod 请求方式
  * @param initialDelay 任务延迟执行的时长
  * @param slot 此任务应该放在的槽位
  * @param carriedTurns 指针扫描几圈后执行(可以理解为该任务的执行触发点),当小于等于0的时候执行
  * @param isCycle  是否为循环任务,默认为不是
  */
case class BusinessTask(id:String,url:String,params:Map[String, String],
                        requestMethod:String,initialDelay:DateToX,slot:Int,
                        carriedTurns: Int,isCycle:Boolean)
/**
  *
  * @param node 当前节点
  * @param nextNode 下一个节点
  * @param taskList 节点集合
  */
case class Bucket(node:String,nextNode:String,taskList: List[BusinessTask])

class DateToX(timeUnit: TimeUnit, duration: Long) {
  def getMilliSecond: Long = timeUnit.toMillis(duration)
}