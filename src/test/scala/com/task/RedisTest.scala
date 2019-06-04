package com.task

import org.junit.Test
import com.task.util.redis.{CacheKeyTool, RedisTimeoutTool, RedisUtil}
import com.typesafe.scalalogging.LazyLogging
import redis.clients.jedis.Jedis

/**
  * @author limeng
  * @date 2019/6/4 10:18
  * @version 1.0
  */
class RedisTest  extends LazyLogging{
  @Test
  def setByLock(): Unit ={
    val libid = "0016B51A502911E7A365A45E60DCD659"
    val value = "8sssssss"
    val cacheKey=CacheKeyTool.generateSingleKVCacheKey("libid", classOf[RedisTest], libid)
    val ru=new RedisUtil
   // val pool = RedisUtil.jedisPool
   // val jedis = pool.getResource
   val canSet = ru.multiLockSetFlag(cacheKey,1000)
   // val canSet:Boolean=RedisTimeoutTool.multiLockSetFlag(cacheKey,jedis,1000)
    try{
      if(canSet){
        //val bool = RedisTimeoutTool.reLockSetFlag(cacheKey,jedis)

        //RedisTimeoutTool.getLockValue(cacheKey,jedis)
      }
    }catch{
      case ex:Exception =>
        this.logger.error(s"RedisTest setByLock error ")
    }
  }

}
object RedisTest{


}
