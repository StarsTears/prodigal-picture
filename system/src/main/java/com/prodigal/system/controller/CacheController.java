package com.prodigal.system.controller;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.prodigal.system.common.BaseResult;
import com.prodigal.system.common.ResultUtils;
import com.prodigal.system.exception.BusinessException;
import com.prodigal.system.exception.ErrorCode;
import com.prodigal.system.manager.CacheManager;
import com.prodigal.system.manager.strategy.CacheContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @create: 2024-12-18 14:36
 * @description: 缓存操作
 **/
@RestController
@RequestMapping("/cache")
public class CacheController {
    @Resource
    private CacheManager cacheManager;
    @PostMapping("/delete")
    public BaseResult<Boolean> deleteCache(String type,String key) {
        final List<String> types = Arrays.asList("local","caffeine","redis");
        if (!types.contains(type)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 缓存上下文
        CacheContext cacheContext = new CacheContext();
        cacheContext.setType(type);
        cacheContext.setKey(key);
        cacheManager.removeCacheValue(cacheContext);

        return ResultUtils.success(true);
    }
}
