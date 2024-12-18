package com.prodigal.system.manager;

import com.prodigal.system.manager.strategy.CacheContext;
import com.prodigal.system.manager.strategy.CacheStrategy;
import com.prodigal.system.manager.strategy.CaffeineCacheStrategy;
import com.prodigal.system.manager.strategy.RedisCacheStrategy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 缓存管理
 **/
@Component
public class CacheManager {
    public void putCacheValue(CacheContext cacheContext) {

    }

    public String getCacheValue(CacheContext cacheContext) {
        String type = cacheContext.getType();
        CacheStrategy cacheStrategy = new RedisCacheStrategy();
        if ("caffeine".equals(type)){
            cacheStrategy = new CaffeineCacheStrategy();
        }
        return cacheStrategy.getCacheValue(cacheContext);
    }
}
