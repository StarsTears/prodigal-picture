package com.prodigal.system.controller;

import com.prodigal.system.annotation.PermissionCheck;
import com.prodigal.system.common.BaseResult;
import com.prodigal.system.common.ResultUtils;
import com.prodigal.system.exception.BizStatus;
import com.prodigal.system.exception.BusinessException;
import com.prodigal.system.manager.CacheManager;
import com.prodigal.system.manager.strategy.CacheContext;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    @PermissionCheck(mustRole = {"admin", "administrator"})
    public BaseResult<Boolean> deleteCache(@RequestParam("type") String type, @RequestParam("key") String key) {
        final List<String> types = Arrays.asList("local","caffeine","redis");
        if (!types.contains(type)){
            throw new BusinessException(BizStatus.PARAMS_ERROR);
        }
        // 缓存上下文
        CacheContext cacheContext = new CacheContext();
        cacheContext.setType(type);
        cacheContext.setKey(key);
        cacheManager.removeCacheValue(cacheContext);

        return ResultUtils.success(true);
    }
}
