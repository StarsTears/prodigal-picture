package com.prodigal.system.manager.strategy;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 缓存策略接口
 **/
public interface CacheStrategy {
    void putCacheValue(CacheContext cacheContext);
    String getCacheValue(CacheContext cacheContext);
}
