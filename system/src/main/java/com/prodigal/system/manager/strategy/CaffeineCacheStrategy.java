package com.prodigal.system.manager.strategy;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 本地 缓存（支持 per-key 可变 TTL）
 **/
@Service
public class CaffeineCacheStrategy implements CacheStrategy {

    /** 默认过期时间：5 分钟 */
    private static final long DEFAULT_TTL_NANOS = TimeUnit.MINUTES.toNanos(5);

    /** 存储 per-key TTL（单位：纳秒），在 put 时写入，expireAfterCreate 读取后清除 */
    private final ConcurrentHashMap<String, Long> ttlMap = new ConcurrentHashMap<>();

    private final Cache<String, String> LOCAL_CACHE = Caffeine.newBuilder()
            .initialCapacity(1024)
            .maximumSize(10000L)
            .expireAfter(new Expiry<String, String>() {
                @Override
                public long expireAfterCreate(String key, String value, long currentTime) {
                    Long nanos = ttlMap.remove(key);
                    return nanos != null && nanos > 0 ? nanos : DEFAULT_TTL_NANOS;
                }

                @Override
                public long expireAfterUpdate(String key, String value, long currentTime, long currentDuration) {
                    return currentDuration;
                }

                @Override
                public long expireAfterRead(String key, String value, long currentTime, long currentDuration) {
                    return currentDuration;
                }
            })
            .build();

    @Override
    public void putCacheValue(CacheContext cacheContext) {
        int expireTime = cacheContext.getCacheExpireTime();
        if (expireTime > 0) {
            TimeUnit unit = cacheContext.getCacheExpireTimeUnit() != null
                    ? cacheContext.getCacheExpireTimeUnit() : TimeUnit.SECONDS;
            ttlMap.put(cacheContext.getKey(), unit.toNanos(expireTime));
        }
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
