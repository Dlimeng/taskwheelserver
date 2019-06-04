package com.task.util.redis;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisCommands;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author limeng
 * @version 1.0
 * @date 2019/6/4 14:25
 */
public class RedisTimeoutTool {
    private static Logger logger = LoggerFactory.getLogger(RedisTimeoutTool.class);

    private RedisTimeoutTool() {}

    public static final int TimeoutSec = 5;

    public static final int ONEWEEK = 60 * 60 * 24 * 7;

    public static final int ONEHOUR = 60 * 60 * 1;

    private static final String LOCK_SUCCESS = "OK";
    private static final String SET_IF_NOT_EXIST = "NX";
    private static final String SET_WITH_EXPIRE_TIME = "PX";

    /**
     * 尝试获取分布式锁
     * @param jedis Redis客户端
     * @param lockKey 锁
     * @param requestId 请求标识
     * @param expireTime 超期时间
     * @return 是否获取成功
     */
    public static boolean tryGetDistributedLock(Jedis jedis, String lockKey, String requestId, int expireTime) {

        String result = jedis.set(lockKey, requestId, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, expireTime);

        return LOCK_SUCCESS.equals(result);
    }


    public static boolean lockSetFlag(String key, Jedis jedis, int timeoutSec) {
        boolean result = false;
        if(StringUtils.isNotBlank(key) && jedis != null && timeoutSec > 0) {
            String timeout = String.valueOf(getTimeStamp(timeoutSec));
            long setResult = jedis.setnx(key, timeout);
            if(setResult > 0) {
                result = true;
            }else {
                boolean isTimeout = firstCheckTimeout(key, jedis);
                if(isTimeout) {
                    boolean reTimeout = reCheckTimeout(key, jedis, timeoutSec);
                    if(reTimeout) {
                        result = true;
                    }
                }
            }
        }
        return result;
    }

    public static boolean multiLockSetFlag(String key, Jedis jedis, int timeoutSec) {
        boolean result = false;
        long cur = System.currentTimeMillis();
        int maxTime = 3;
        do {
            result = lockSetFlag(key, jedis, timeoutSec);
            if(!result) {
                maxTime--;
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }while (maxTime > 0 && !result);

        return result;
    }

    public static boolean reLockSetFlag(String key, Jedis jedis) {
        Boolean result=false;
        if(StringUtils.isNotBlank(key) && jedis != null) {
            result = jedis.del(key) > 0;
        }
        return result;
    }

    public static String getLockValue(String key, Jedis jedis) {
        String value = null;
        if(StringUtils.isNotBlank(key) && jedis != null) {
            value = jedis.get(key);
        }
        return value;
    }

    public static String setLockValue(String key,String value  ,Jedis jedis) {
        String result = null;
        if(StringUtils.isNotBlank(key) && jedis != null) {
            value = jedis.set(key,value);
        }
        return result;
    }

    private static long getTimeStamp(int incSec) {
        long current = System.currentTimeMillis() / 1000 + incSec;
        return current;
    }

    private static boolean hasTimeout(long timestamp) {
        return (System.currentTimeMillis() / 1000) > timestamp;
    }

    private static boolean firstCheckTimeout(String key, Jedis jedis) {
        boolean timeout = true;
        if(StringUtils.isNotBlank(key) && jedis != null) {
            long newTimeStamp = 0;
            String newTimeout = jedis.get(key);
            if(StringUtils.isNotBlank(newTimeout)) {
                try{
                    newTimeStamp = Long.parseLong(newTimeout);
                }catch (NumberFormatException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            timeout = hasTimeout(newTimeStamp);
        }
        return timeout;
    }

    private static boolean reCheckTimeout(String key, Jedis jedis, int timeoutSec) {
        boolean timeout = true;
        if(StringUtils.isNotBlank(key) && jedis != null) {
            long newTimeStamp = 0;
            String newTimeout = jedis.getSet(key, String.valueOf(getTimeStamp(timeoutSec)));
            if(StringUtils.isNotBlank(newTimeout)) {
                try{
                    newTimeStamp = Long.parseLong(newTimeout);
                }catch (NumberFormatException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            timeout = hasTimeout(newTimeStamp);
        }
        return timeout;
    }


}
