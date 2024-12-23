package com.prodigal.system.manager;

import com.prodigal.system.constant.CacheConstant;
import com.prodigal.system.manager.strategy.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 缓存管理
 **/
@Component
public class CacheManager {
    @Autowired
    private RedisTemplate redisTemplate;
    @Resource
    private RedisCacheStrategy redisCacheStrategy;
    @Resource
    private CaffeineCacheStrategy caffeineCacheStrategy;
    public void putCacheValue(CacheContext cacheContext) {
        String type = cacheContext.getType();
        CacheStrategy cacheStrategy =redisCacheStrategy;
        if (CacheConstant.CAFFEINE_TYPE.equals(type)){
            cacheStrategy = caffeineCacheStrategy;
        }
        cacheStrategy.putCacheValue(cacheContext);
    }

    public Object getCacheValue(CacheContext cacheContext) {
        String type = cacheContext.getType();
        CacheStrategy cacheStrategy = redisCacheStrategy;
        if (CacheConstant.CAFFEINE_TYPE.equals(type)){
            cacheStrategy = caffeineCacheStrategy;
        }
        return cacheStrategy.getCacheValue(cacheContext);
    }
    public void removeCacheValue(CacheContext cacheContext) {
        String type = cacheContext.getType();
        CacheStrategy cacheStrategy = redisCacheStrategy;
        if (CacheConstant.CAFFEINE_TYPE.equals(type)){
            cacheStrategy = caffeineCacheStrategy;
        }
        cacheStrategy.removeCacheValue(cacheContext);
    }
}
