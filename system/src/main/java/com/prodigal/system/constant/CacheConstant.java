package com.prodigal.system.constant;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 缓存key 常量
 **/
public interface CacheConstant {
    /**
     * 项目前缀
     */
    String PROJECT_NAME = "picture";
    /**
     * 缓存key
     */
    String PICTURE_PAGE_REDIS_CACHE_KEY = "picture:listPictureVOByPage:";
    String PICTURE_PAGE_CAFFEINE_CACHE_KEY = "listPictureVOByPage:";
}
