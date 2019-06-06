package com.task.model

import java.util.concurrent.TimeUnit

import com.typesafe.scalalogging.LazyLogging


/**
  * @author limeng
  * @date 2019/5/30 18:57
  * @version 1.0
  */
object TimerTaskList extends LazyLogging{




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