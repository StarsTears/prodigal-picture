package com.prodigal.system.manager.strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: Redis 缓存
 **/
@Service
public class RedisCacheStrategy implements CacheStrategy{
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public void putCacheValue(CacheContext cacheContext) {
        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
        String key = cacheContext.getKey();
        String value = cacheContext.getValue();
        int cacheExpireTime = cacheContext.getCacheExpireTime();
        TimeUnit cacheExpireTimeUnit = cacheContext.getCacheExpireTimeUnit();
        opsForValue.set(key,value,cacheExpireTime,cacheExpireTimeUnit);
    }

    @Override
    public String getCacheValue(CacheContext cacheContext) {
        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
        String key = cacheContext.getKey();
        return opsForValue.get(key);
    }
}
