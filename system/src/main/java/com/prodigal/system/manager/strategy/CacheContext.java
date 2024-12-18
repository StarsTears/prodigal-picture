package com.prodigal.system.manager.strategy;

import lombok.Data;

import java.util.concurrent.TimeUnit;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 缓存上下文(用于定义在策略模式中传递的参数)
 **/
@Data
public class CacheContext {
    private String type;
    private String key;
    private String value;
    private int cacheExpireTime;
    private TimeUnit cacheExpireTimeUnit = TimeUnit.SECONDS; //默认秒
}
