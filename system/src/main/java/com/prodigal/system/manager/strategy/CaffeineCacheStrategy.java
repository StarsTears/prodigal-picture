package com.prodigal.system.manager.strategy;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 本地 缓存
 **/
@Service
public class CaffeineCacheStrategy implements CacheStrategy{
    private final Cache<String, String> LOCAL_CACHE = Caffeine.newBuilder()
                                                                .initialCapacity(1024)
                                                                .maximumSize(10000L)
                                                                    // 缓存 5 分钟移除
                                                                .expireAfterWrite(5L, TimeUnit.MINUTES)
                                                                .build();

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public void putCacheValue(CacheContext cacheContext) {

    }

    @Override
    public String getCacheValue(CacheContext cacheContext) {
        String key = cacheContext.getKey();
        String cachedValue = stringRedisTemplate.opsForValue().get(key);
        return null;
    }
}
