package com.task.service

import akka.actor.Actor
import com.task.model.{Bucket, BusinessTask, TimerTaskConfigs}
import com.task.util.redis.RedisUtil
import com.typesafe.scalalogging.LazyLogging
import org.json4s.{DefaultFormats, NoTypeHints}
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization._
/**
  * 任务环
  * @Author: limeng
  * @Date: 2019/6/22 18:44
  */
class TimeTaskRound extends Actor with LazyLogging{
  override def receive: Receive = ???
}
object TimeTaskRound extends LazyLogging{
  private val taskConfig = TimerTaskConfigs.apply
  /**
    * 根据地址获取曹格
    * @param pointerAddress 指针指向的地址
    * @return
    */
  def  getCurrentRing(pointerAddress:String,redisUtil:RedisUtil):Bucket={
    val ringString=redisUtil.get(pointerAddress)
    //初始化
    if(ringString.isEmpty){
       val keyPrefix =taskConfig.keyPrefix
       val tmp:Int = pointerAddress.substring(keyPrefix.length, pointerAddress.length).toInt
       val nextNode:String = {
         var rs:Int =0
         if(tmp == (taskConfig.grooveNumber -1)){
           rs=0
         }else{
           rs=tmp + 1
         }
         keyPrefix + rs
       }
      Bucket(pointerAddress,nextNode,null)
    }else{
      implicit val formats = DefaultFormats
      parse(ringString).extract[Bucket]
    }
  }

  /**
    * 执行任务队列
    * @param businessTasks
    * @param redisUtil
    * @return
    */
  def producerTask(businessTasks: List[BusinessTask],redisUtil:RedisUtil):Long={
    implicit val formats = Serialization.formats(NoTypeHints)
    val values=businessTasks.map(f=>{
      write(f)
    }).toArray
    redisUtil.lpush(taskConfig.channel,values)
  }
}