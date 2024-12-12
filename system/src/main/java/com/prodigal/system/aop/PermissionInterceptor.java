package com.prodigal.system.aop;

import com.prodigal.system.annotation.PermissionCheck;
import com.prodigal.system.exception.BusinessException;
import com.prodigal.system.exception.ErrorCode;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.model.enums.UserRoleEnum;
import com.prodigal.system.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 角色校验
 **/
@Aspect
@Component
@Slf4j
public class PermissionInterceptor {
    @Resource
    private UserService userService;

    /**
     * 角色（权限）校验
     * @param joinPoint 切入点
     * @param permission 角色（权限）注解
     * @return 执行结果
     * @throws Throwable
     */
    @Around("@annotation(permission)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, PermissionCheck permission) throws Throwable {
        String[] mustRole = permission.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        //当前登录用户
        User user = userService.getLoginUser(request);
        //TODO:有多个角色，这里需要进行匹配
        Set<UserRoleEnum> mustRoleEnums = Arrays.stream(mustRole).map(UserRoleEnum::getEnumByValue).collect(Collectors.toSet());
        //不需要的,放行
        if (CollectionUtils.isEmpty(mustRoleEnums)){
            return joinPoint.proceed();
        }
        //验权
        //获取当前用户的角色（权限）
        Set<UserRoleEnum> userRoleEnums = getRolesConvertSet(user.getUserRole());
        if (CollectionUtils.isEmpty(userRoleEnums)){
            throw new BusinessException(ErrorCode.USER_NOT_PERMISSION);
        }
        //使用当前用户的角色只要有一个匹配上，就放行
        // 使用 Stream 判断是否有任何元素相等
        boolean result = mustRoleEnums.stream()
                .anyMatch(userRoleEnums::contains);
        if (!result){
            throw new BusinessException(ErrorCode.USER_NOT_PERMISSION);
        }
        return joinPoint.proceed();
    }

    /**
     * 获取权限 转为 list 集合
     * @param role 权限
     * @return 权限集合
     */
    private Set<UserRoleEnum> getRolesConvertSet(String role){
        if (role == null || role.trim().isEmpty()) {
            // 如果输入的 role 为 null 或空字符串，可以选择抛出异常，或者返回空集合
            return Collections.emptySet();  // 也可以选择抛出 IllegalArgumentException
        }
        List<String> mustRoles;
        if (role.contains(",")){
            mustRoles = Arrays.stream(role.split(",")).map(String::trim).collect(Collectors.toList());
        }else{
            mustRoles = Collections.singletonList(role.trim());
        }
        return mustRoles.stream().map(UserRoleEnum::getEnumByValue).filter(Objects::nonNull).collect(Collectors.toSet());
    }
}
