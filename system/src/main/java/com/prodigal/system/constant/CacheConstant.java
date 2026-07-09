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
    /** 验证码消息幂等消费标记 */
    String CAPTCHA_CONSUMED_PREFIX = "captcha:consumed:";
    /** 邮件发送消息幂等消费标记 */
    String EMAIL_SEND_CONSUMED_PREFIX = "email:send:consumed:";
    long CODE_EXPIRE_MINUTES = 5;

    long SEND_LOCK_SECONDS = 60;

    /*********登录失败限制************/
    String LOGIN_FAIL_PREFIX = "login:fail:";
    /** 失败次数阈值，达到后需要验证码 */
    long LOGIN_FAIL_MAX_COUNT = 3L;
    /** 失败计数器有效期（分钟） */
    long LOGIN_FAIL_EXPIRE_MINUTES = 15;

    /*********接口限流************/
    String RATE_LIMIT_PREFIX = "rate:limit:";

    /*********图片上传幂等锁************/
    String UPLOAD_LOCK_PREFIX = "picture:upload:lock:";
    long UPLOAD_LOCK_SECONDS = 300;


}
