package com.task.service


import akka.actor.Actor
import com.task.model.{BusinessTask, DateToX, TimerTaskConfigs}
import com.task.util.redis.RedisUtil
import com.typesafe.scalalogging.LazyLogging
import org.json4s.DefaultFormats
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
/**
  * 发布
  *
  * @author limeng
  * @date 2019/5/30 19:58
  * @version 1.0
  */
class TimeTaskPublish extends Actor with LazyLogging{
  private val redisUtil = new RedisUtil
  private val taskConfig = TimerTaskConfigs.apply
  override def receive = {
    case "start" =>{
      //发布任务的地址
      val task=redisUtil.rpop(taskConfig.hashedWheelProducerTask)
      if(task.nonEmpty){
        implicit val formats = DefaultFormats
        val businessTask:BusinessTask = parse(task).extract[BusinessTask]

      }
    }
  }

  def schedule(url: String, params: Map[String, String], requestMethod: RequestMethod, initialDelay: DateToX): Unit ={

  }
}
