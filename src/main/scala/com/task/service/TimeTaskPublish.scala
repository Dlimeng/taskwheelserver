package com.task.service




import akka.actor.Actor
import com.task.commn.RequestMethodEnumer
import com.task.model.{BusinessTask, DateToX, TimerTaskConfigs}
import com.task.util.RandomGUIDUtils
import com.task.util.redis.RedisUtil
import com.typesafe.scalalogging.LazyLogging
import org.json4s.{DefaultFormats, NoTypeHints}
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization._
import scala.util.control.Breaks._
/**
  * 发布任务
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
        this.schedule(businessTask, redisUtil)
      }else{
        Thread.sleep(1000)
      }
    }
  }

  def schedule(businessTask:BusinessTask,redisUtil:RedisUtil): Unit ={
      if(businessTask != null && redisUtil != null){
        val id:String=new RandomGUIDUtils().toString
        var pointerAddress = this.getPointerAddress(redisUtil)
        //获取地址下标
        val pointer = this.getPointerAddressSubscript(pointerAddress)
        val dle = java.util.concurrent.TimeUnit.MILLISECONDS.toSeconds(businessTask.initialDelay.getMilliSecond)

        val newBusinessTask = BusinessTask(id,businessTask.url,businessTask.params,RequestMethodEnumer.getMethod(businessTask.requestMethod)
          ,businessTask.initialDelay,this.getSolt(pointerAddress, dle.toInt), this.getCarriedTurns(businessTask.initialDelay.getMilliSecond),
          isCycle = false
        )
        this.logger.info(s"发布任务 businessTask:$businessTask")
        //如果应该放的槽位和当前指针地址一致 且 执行环数为0的时候则直接发布到执行队列
        if(newBusinessTask.slot == pointer && newBusinessTask.carriedTurns == 0){
          this.producerTask(newBusinessTask,redisUtil)
          return
        }

        pointerAddress = taskConfig.keyPrefix + newBusinessTask.slot
        this.producerTask(pointerAddress,newBusinessTask,redisUtil)
      }
  }

  private def producerTask(businessTask:BusinessTask,redisUtil:RedisUtil):Long={
    TimeTaskRound.producerTask(List(businessTask),redisUtil)
  }

  private def producerTask(pointerAddress: String, businessTask: BusinessTask, redisUtil:RedisUtil):Unit={
    val pointerAddressLock:String = pointerAddress + "lock"
    breakable {
      while (true) {
        try {
          val dateToX = new DateToX(java.util.concurrent.TimeUnit.SECONDS, 5).getMilliSecond
          val isOk = redisUtil.multiLockSetFlag(pointerAddressLock, dateToX.toInt)
          if (isOk) {
            val bucket = TimeTaskRound.getCurrentRing(pointerAddress, redisUtil)
            val tasks: List[BusinessTask] = bucket.taskList.::(businessTask)
            val newBucket = bucket.copy(taskList = tasks)

            implicit val formats = Serialization.formats(NoTypeHints)
            val isOk2 = redisUtil.set(pointerAddress, write(newBucket))

            if ("OK".equals(isOk2)) {
              val isOkLong = redisUtil.del(pointerAddressLock)
              if (isOkLong == 1) break
            }
          }
        } catch {
          case ex: Exception => this.logger.error(ex.getMessage)
        }
      }
    }
  }
  /**
    * 获取当前指针的位置
    * @param redisUtil
    * @return
    */
  private def getPointerAddress(redisUtil:RedisUtil): String ={
    redisUtil.get(taskConfig.pointer)
  }

  /**
    * 获取应该放在的曹格的下标
    * @param pointer 下标地址
    * @param delayDate 延迟时间
    *
    * @return
    */
  private def getSolt(pointer:Int,delayDate:Int):Int={
    val newPointer = pointer + 1
    val newDelayDate = delayDate / taskConfig.lockOutTime
    var slot:Int = 0
    val grooveNumber=taskConfig.grooveNumber
    slot = {
      slot = newPointer + newDelayDate % grooveNumber
      if(slot > grooveNumber) {
        slot = slot - grooveNumber
      } else {
        if(slot < 0){
          slot = slot * -1
        }
      }
      slot
    }.-(1)
    slot
  }

  /**
    * 从地址中获取下标key
    * @return
    */
  private def getPointerAddressSubscript(pointerAddress: String): Int ={
    val point:Int=pointerAddress.substring(taskConfig.keyPrefix.length,pointerAddress.length).toInt
    if(point == 0) taskConfig.grooveNumber-1
    else point-1
  }

  /**
    * 获取应该放在的槽格的下标
    * @param pointer 下标地址
    * @param delayDate 延迟时间单位毫秒
    * @return
    */
  private def getSolt(pointer: String, delayDate: Int):Int= this.getSolt(this.getPointerAddressSubscript(pointer), delayDate)

  /**
    * 指针扫描几圈后执行(可以理解为该任务的执行触发点),当小于等于0的时候执行
    * @param milliSecond 毫秒
    * @return
    */
  private def getCarriedTurns(milliSecond:Long): Int = (milliSecond/(new DateToX(TimerTaskConfigs.timeUnit, taskConfig.grooveNumber).getMilliSecond * taskConfig.lockOutTime)).toInt
}
