package com.prodigal.system.constant;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 缓存key 常量
 **/
public interface CacheConstant {
    String CAFFEINE_TYPE = "caffeine";
    String REDIS_TYPE = "redis";
    /**
     * 项目前缀
     */
    String PROJECT_NAME = "picture";
    /**
     * 缓存key
     */
    String PICTURE_PAGE_REDIS_CACHE_KEY = "picture:listPictureVOByPage:";
    String PICTURE_PAGE_CAFFEINE_CACHE_KEY = "listPictureVOByPage:";

    /*********邮箱验证码************/
    String CODE_PREFIX = "verification:code:";
    /** 发送频率限制（与验证码有效期解耦，避免 5 分钟内无法重发） */
    String SEND_LOCK_PREFIX = "verification:send:lock:";
    long CODE_EXPIRE_MINUTES = 5;

    long SEND_LOCK_SECONDS = 60;


}
