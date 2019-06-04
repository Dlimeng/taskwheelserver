package com.task.util
import java.util.logging.Logger

import redis.clients.jedis._

import scala.collection.JavaConversions._
import com.task.util.RedisUtil.clients
import org.apache.commons.lang.StringUtils
import com.typesafe.scalalogging._
/**
  * @author limeng
  * @date 2019/5/30 23:54
  * @version 1.0
  */
class RedisUtil  extends LazyLogging with Serializable{

  val TimeoutSec = 5

  val ONEWEEK: Int = 60 * 60 * 24 * 7

  val ONEHOUR: Int = 60 * 60 * 1

  private val LOCK_SUCCESS = "OK"
  private val SET_IF_NOT_EXIST = "NX"
  private val SET_WITH_EXPIRE_TIME = "PX"


  /**
    *
    * @param key
    * @param value
    * @return
    */

  def set(key:String,value:Any): Unit ={

    clients.set(key,String.valueOf(value))

  }

  /**
    *
    * @param key
    * @param time
    * @return
    */

  def hget(key:String,time:Long): Option[String] ={

    val value=clients.hget(key,String.valueOf(time))
    Option(value)
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

  /**
    *
    * @param key
    * @param time
    * @return
    */
  def hdel(key:String,time:Any): Option[Long] ={

    Some(clients.hdel(key,String.valueOf(time)))

  }

  /**
    *
    * @param key
    * @param times
    * @return
    */
  def rpush(key:String,times:Any): Option[Long] ={

    Some(clients.rpush(key,String.valueOf(times)))

  }


  /**
    *
    * @param key
    * @return
    */
  def lpop(key:String): Option[Long] ={

    val time=clients.lpop(key)
    if(time==null)
      None
    else
      Some(time.toLong)
  }


  /**
    *
    * @param key
    * @return
    */
  def lhead(key:String): Option[Long] ={

    val head=clients.lindex(key,0)
    if(head==null)
      None
    else
      Some(head.toLong)
  }

  //----------------------------------------------------分布式锁----------------------------------------------------------//
  /**
    * 尝试获取分布式锁
    *
    * @param jedis      Redis客户端
    * @param lockKey    锁
    * @param requestId  请求标识
    * @param expireTime 超期时间
    * @return 是否获取成功
    */
  def tryGetDistributedLock(jedis: Nothing, lockKey: String, requestId: String, expireTime: Int): Boolean = {
    val result = clients.set(lockKey, requestId, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, expireTime)
    LOCK_SUCCESS == result
  }


  def lockSetFlag(key: String,  timeoutSec: Int): Boolean = {
    var result = false
    if (StringUtils.isNotBlank(key) && clients != null && timeoutSec > 0) {
      val timeout = String.valueOf(getTimeStamp(timeoutSec))
      val setResult = clients.setnx(key, timeout)
      if (setResult > 0) result = true
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

  def multiLockSetFlag(key: String, jedis: Nothing, timeoutSec: Int): Boolean = {
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

  def reLockSetFlag(key: String, jedis: Nothing): Unit = {
    if (StringUtils.isNotBlank(key) && jedis != null) clients.del(key)
  }

  def getLockValue(key: String, jedis: Nothing): String = {
    var value:String = null
    if (StringUtils.isNotBlank(key) && jedis != null) {
      value = clients.get(key)
    }
    value
  }

  private def getTimeStamp(incSec: Int) = {
    val current = System.currentTimeMillis / 1000 + incSec
    current
  }

  private def hasTimeout(timestamp: Long) = (System.currentTimeMillis / 1000) > timestamp

  private def firstCheckTimeout(key: String) = {
    var timeout = true
    if (StringUtils.isNotBlank(key) && clients != null) {
      var newTimeStamp:Long = 0
      val newTimeout = clients.get(key)
      if (StringUtils.isNotBlank(newTimeout)) try
        newTimeStamp = newTimeout.toLong
      catch {
        case e: NumberFormatException =>
          logger.error(e.getMessage, e)
      }
      timeout = hasTimeout(newTimeStamp)
    }
    timeout
  }

  private def reCheckTimeout(key: String, timeoutSec: Int) = {
    var timeout = true
    if (StringUtils.isNotBlank(key) && clients != null) {
      var newTimeStamp:Long = 0
      val newTimeout = clients.getSet(key, String.valueOf(getTimeStamp(timeoutSec)))
      if (StringUtils.isNotBlank(newTimeout)) {
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
  private val jedisClusterNodes = new java.util.HashSet[HostAndPort]()
  private val properties = PropertiesUtils.apply("redis.properties")
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
  val clients = new JedisCluster(jedisClusterNodes,config)

}
