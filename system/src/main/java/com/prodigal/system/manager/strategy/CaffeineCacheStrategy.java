package com.prodigal.system.manager.strategy;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

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
    @Override
    public void putCacheValue(CacheContext cacheContext) {
        LOCAL_CACHE.put(cacheContext.getKey(), cacheContext.getValue());
    }

    @Override
    public Object getCacheValue(CacheContext cacheContext) {
        return LOCAL_CACHE.getIfPresent(cacheContext.getKey());
    }

    @Override
    public void removeCacheValue(CacheContext cacheContext) {
        LOCAL_CACHE.invalidate(cacheContext.getKey());
    }

}
