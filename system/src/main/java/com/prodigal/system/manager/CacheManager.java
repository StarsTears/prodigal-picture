package com.prodigal.system.manager;

import com.prodigal.system.constant.CacheConstant;
import com.prodigal.system.manager.strategy.CacheContext;
import com.prodigal.system.manager.strategy.CacheStrategy;
import com.prodigal.system.manager.strategy.CaffeineCacheStrategy;
import com.prodigal.system.manager.strategy.RedisCacheStrategy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 缓存管理
 **/
@Component
public class CacheManager {
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
