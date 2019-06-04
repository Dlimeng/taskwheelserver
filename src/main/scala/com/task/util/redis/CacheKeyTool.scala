package com.task.util.redis

/**
  * @author limeng
  * @date 2019/6/4 10:35
  * @version 1.0
  */
object CacheKeyTool {
    def generateSingleCacheKey(baseClass: Class[_], id: String):String= String.format("sg:%s:%s", baseClass.getSimpleName, id)

    def generateSingleLockCacheKey(baseClass: Class[_], id: String):String= generateSingleKVCacheKey("lock", baseClass, id);

  def generateSingleKVCacheKey(baseClass: Class[_], id: String):String = generateSingleKVCacheKey("kv", baseClass, id)

  def generateSingleKVCacheKey(pre: String, baseClass: Class[_], key: String):String = String.format("%s:sg:%s:%s", pre, baseClass.getSimpleName, key)

  def generateSetCacheKey(baseClass: Class[_], key: String):String = String.format("set:%s:%s", baseClass.getSimpleName, key)
}
