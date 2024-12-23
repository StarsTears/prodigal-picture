package com.prodigal.system.manager.strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: Redis缓存实现类
 **/
@Primary
@Service
public class RedisCacheStrategy implements CacheStrategy {
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void putCacheValue(CacheContext cacheContext) {
        String key = cacheContext.getKey();
        String value = cacheContext.getValue();
        int cacheExpireTime = cacheContext.getCacheExpireTime();
        TimeUnit cacheExpireTimeUnit = cacheContext.getCacheExpireTimeUnit();
        redisTemplate.opsForValue().set(key, value, cacheExpireTime, cacheExpireTimeUnit);
    }

    @Override
    public Object getCacheValue(CacheContext cacheContext) {
        String key = cacheContext.getKey();
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void removeCacheValue(CacheContext cacheContext) {
        redisTemplate.delete(cacheContext.getKey());
    }
}
