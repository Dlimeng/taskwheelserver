package com.task.util.redis

import com.task.util.redis.RedisUtil.jedisPool
import com.task.util.{PropertiesUtils, RandomGUIDUtils}
import com.typesafe.scalalogging._
import redis.clients.jedis.{JedisPool, _}

import scala.collection.JavaConversions._
/**
  * @author limeng
  * @date 2019/5/30 23:54
  * @version 1.0
  */
class RedisUtil  extends LazyLogging with Serializable{
  lazy val clients = jedisPool.getResource

  val TimeoutSec = 5

  val ONEWEEK: Int = 60 * 60 * 24 * 7

  val ONEHOUR: Int = 60 * 60 * 1

  private val LOCK_SUCCESS = "OK"
  private val SET_IF_NOT_EXIST:String = "NX"
  private val SET_WITH_EXPIRE_TIME:String = "PX"


  /**
    *
    * @param key
    * @param value
    * @return
    */

  def set(key:String,value:Any): String ={
    clients.set(key,String.valueOf(value))
  }

  def get(key:String): String ={
    clients.get(key)
  }
  /**
    *
    * @param key
    * @param time
    * @return
    */

  def hget(key:String,time:Long): String ={
    clients.hget(key,String.valueOf(time))
  }

  /**
    *
    * @param key
    * @param time
    * @param value
    * @return
    */
  def hset(key:String,time:Long,value:Any): Boolean ={
    clients.hset(key,String.valueOf(time),String.valueOf(value))==1

  }

  def hmset(key:String,map:Map[Long,String]): Unit ={

    val map2=Map[String,String]()
    map.foreach{case (key:Long,value:String)=>
      map2.put(key.toString,value)
    }

    clients.hmset(key,mapAsJavaMap(map2))

  }

  def del(key:String): Long ={
    clients.del(key)
  }

  /**
    *
    * @param key
    * @param time
    * @return
    */
  def hdel(key:String,time:Any): Long ={
    clients.hdel(key,String.valueOf(time))
  }

  /**
    *
    * @param key
    * @param times
    * @return
    */
  def rpush(key:String,times:Any): Long ={
    clients.rpush(key,String.valueOf(times))
  }

  def lpush(key: String, strings: Any): Long ={
    clients.lpush(key,String.valueOf(strings))
  }
  /**
    *
    * @param key
    * @return
    */
  def lpop(key:String): String ={

    val time=clients.lpop(key)
    if(time==null)
      null
    else
      time
  }

  def rpop(key:String):String={
    clients.rpop(key)
  }

  /**
    *
    * @param key
    * @return
    */
  def lhead(key:String): String ={
    val head=clients.lindex(key,0)
    if(head==null)
      null
    else
      head
  }

  //----------------------------------------------------分布式锁----------------------------------------------------------//
  /**
    * 尝试获取分布式锁
    *
    * @param lockKey    锁
    * @param expireTime 超期时间
    * @return 是否获取成功
    */
  def tryGetDistributedLock(lockKey: String, expireTime: Int): Boolean = {
    val uuid:String = new RandomGUIDUtils().toString
    val time:Long=expireTime.toLong
    val result:String = clients.set(lockKey, uuid, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, time)
    LOCK_SUCCESS == result
  }


  def lockSetFlag(key: String,timeoutSec: Int): Boolean = {
    var result = false
    if (key.nonEmpty  && timeoutSec > 0) {
      val timeout = getTimeStamp(timeoutSec).toString
      val setResult = tryGetDistributedLock(key,timeoutSec)
      if (setResult) result = true
      else {
        val isTimeout = firstCheckTimeout(key)
        if (isTimeout) {
          val reTimeout = reCheckTimeout(key,  timeoutSec)
          if (reTimeout) result = true
        }
      }
    }
    result
  }

  def multiLockSetFlag(key: String,timeoutSec: Int): Boolean = {
    var result = false
    val cur = System.currentTimeMillis
    var maxTime = 3
    do {
      result = lockSetFlag(key, timeoutSec)
      if (!result) {
        maxTime -= 1
        try
          Thread.sleep(500)
        catch {
          case e: InterruptedException =>
            logger.error(e.getMessage, e)
        }
      }
    } while ( {
      maxTime > 0 && !result
    })
    result
  }

  def reLockSetFlag(key: String): Unit = {
    if (key.nonEmpty) clients.del(key)
  }

  def getLockValue(key: String): String = {
    clients.get(key)
  }

  private def getTimeStamp(incSec: Int):Long= {
     System.currentTimeMillis / 1000 + incSec
  }

  private def hasTimeout(timestamp: Long):Boolean = (System.currentTimeMillis / 1000) > timestamp

  private def firstCheckTimeout(key: String):Boolean = {
    var timeout = true
    if (key.nonEmpty) {
      var newTimeStamp:Long = 0
      val newTimeout = clients.get(key)
      if (newTimeout.nonEmpty){
        try
          newTimeStamp = newTimeout.toLong
        catch {
          case e: NumberFormatException =>
            logger.error(e.getMessage, e)
        }
      }
      timeout = hasTimeout(newTimeStamp)
    }
    timeout
  }

  private def reCheckTimeout(key: String, timeoutSec: Int):Boolean = {
    var timeout = true
    if (key.nonEmpty) {
      var newTimeStamp:Long = 0

      val newTimeout =clients.getSet(key, getTimeStamp(timeoutSec).toString)
      if (newTimeout.nonEmpty) {
        try
          newTimeStamp = newTimeout.toLong
        catch {
          case e: NumberFormatException =>
            logger.error(e.getMessage, e)
        }
      }
      timeout = hasTimeout(newTimeStamp)
    }
    timeout
  }
}

object RedisUtil {

  private val properties = PropertiesUtils.apply("/redis.properties")
  private val addr = properties.getProperty("redis.addr")
  private val port = properties.getProperty("redis.port").toInt
  private val auth = properties.getProperty("redis.auth")
  private val maxIdle = properties.getProperty("redis.maxIdle").toInt
  private val maxActive = properties.getProperty("redis.maxActive").toInt
  private val maxWait = properties.getProperty("redis.maxWait").toInt
  private val timeOut = properties.getProperty("redis.timeOut").toInt
  private val testOnBorrow = properties.getProperty("redis.testOnBorrow").toBoolean
  val config = new JedisPoolConfig

  config.setMaxTotal(maxActive)
  config.setMaxIdle(maxIdle)
  config.setMaxWaitMillis(maxWait)
  config.setTestOnBorrow(testOnBorrow)
  val jedisPool = new  JedisPool(config, addr, port, maxWait, auth)
}
