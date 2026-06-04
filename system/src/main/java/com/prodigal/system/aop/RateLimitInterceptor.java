package com.prodigal.system.aop;

import com.prodigal.system.annotation.RateLimit;
import com.prodigal.system.exception.BusinessException;
import com.prodigal.system.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
public class RateLimitInterceptor {

    @Resource
    private RedisTemplate redisTemplate;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }
        String ip = attributes.getRequest().getRemoteAddr();
        String key = rateLimit.prefix() + joinPoint.getSignature().toShortString() + ":" + ip;
        long count = redisTemplate.opsForValue().increment(key);
        if (count == 1) {
            redisTemplate.expire(key, rateLimit.window(), rateLimit.timeUnit());
        }
        if (count > rateLimit.maxRequests()) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS);
        }
        return joinPoint.proceed();
    }
}
