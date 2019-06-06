package com.task.model



import com.task.util.PropertiesUtils
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration.TimeUnit

/**
  * @author limeng
  * @date 2019/6/4 15:27
  * @version 1.0
  */
/**
  *
  * @param corePoolSize 线程数量
  * @param encode 请求编码格式
  * @param token 校验token(哈希值)
  * @param testIp 测试环境ip地址
  * @param channel  执行器生产消费者地址(执行队列)
  * @param channelTmp 执行器生产消费者地址(备份队列,如果执行队列执行失败,则从备份队列中重新获取,并添加到执行队列)
  * @param grooveNumber 槽的数量
  * @param keyPrefix 在容器中存储的key值前缀
  * @param ringCorePoolSize 线程最大数量
  * @param initialDelay 循环时距离上次执行开始时的间隔
  * @param lockKey 指针锁的key值
  * @param pointer 指针当前所指向的地址,从redis中获取(移动指针时必须为原子操作),还需要在项目启动时定位上次指针的位置
  * @param period 遍历环形结构的时间间隔
  * @param lockOutTime 锁的时间
  * @param hashedWheelProducerTask 发布任务消费消费者地址
  * @param hashedWheelProducerTaskTmp 发布任务消费消费者备用队列地址
  * @param hashedWheelLogPrefix 前缀
  * @param resultCodeName  判断任务是否执行成功的字段的名称
  */
case class TimerTaskConfig(corePoolSize:Int,encode:String,token:String,testIp:String,channel:String
                          ,channelTmp:String,grooveNumber:Int,keyPrefix:String,ringCorePoolSize:Int,initialDelay:Int,
                           lockKey:String,pointer:String,period:Int,lockOutTime:Int,hashedWheelProducerTask:String,
                           hashedWheelProducerTaskTmp:String,hashedWheelLogPrefix:String,resultCodeName:String)

object TimerTaskConfigs extends LazyLogging{
  val timeUnit:TimeUnit =java.util.concurrent.TimeUnit.SECONDS
  val POST:String = "POST"
  val GET:String = "GET"

  def apply: TimerTaskConfig ={
    val propertiesUtil =PropertiesUtils.apply()
    val corePoolSize = propertiesUtil.getProperty("timer.core.pool.size").toInt
    val encode = propertiesUtil.getProperty("timer.encode")
    val token = propertiesUtil.getProperty("timer.token")
    //val testIp = propertiesUtil.getProperty("timer.test.ip")
    val channel = propertiesUtil.getProperty("timer.channel")
    val channelTmp = propertiesUtil.getProperty("timer.channel.tmp")
    val grooveNumber = propertiesUtil.getProperty("timer.groove.number").toInt
    val keyPrefix = propertiesUtil.getProperty("timer.key.prefix")
    val ringCorePoolSize = propertiesUtil.getProperty("timer.ring.core.pool.size").toInt
    val initialDelay = propertiesUtil.getProperty("timer.initial.delay").toInt
    val lockKey = propertiesUtil.getProperty("timer.lock.key")
    val pointer = propertiesUtil.getProperty("timer.pointer")
    val lockOutTime = propertiesUtil.getProperty("timer.lock.out.time").toInt
    val period = propertiesUtil.getProperty("timer.period").toInt
    val hashedWheelProducerTask = propertiesUtil.getProperty("timer.hashed.wheel.producer.task")
    val hashedWheelProducerTaskTmp = propertiesUtil.getProperty("timer.hashed.wheel.producer.task.tmp")
    val hashedWheelLogPrefix = propertiesUtil.getProperty("timer.hashed.wheel.log.prefix")
    val resultCodeName = propertiesUtil.getProperty("timer.result.code.name")
    val testIp:String=""

    TimerTaskConfig(corePoolSize,encode,token,testIp,channel
      ,channelTmp,grooveNumber,keyPrefix,ringCorePoolSize,initialDelay,
      lockKey,pointer,period,lockOutTime,hashedWheelProducerTask,
      hashedWheelProducerTaskTmp,hashedWheelLogPrefix,resultCodeName)
  }

}
