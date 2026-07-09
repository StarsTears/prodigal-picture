package com.prodigal.system.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    /** 时间窗口内最大请求数 */
    int maxRequests() default 10;
    /** 时间窗口 */
    int window() default 60;
    /** 时间单位 */
    TimeUnit timeUnit() default TimeUnit.SECONDS;
    /** 限流标识前缀 */
    String prefix() default "rate:limit:";
}
