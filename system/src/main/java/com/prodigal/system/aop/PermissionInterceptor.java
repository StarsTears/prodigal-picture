package com.prodigal.system.aop;

import com.prodigal.system.annotation.PermissionCheck;
import com.prodigal.system.exception.BusinessException;
import com.prodigal.system.exception.ErrorCode;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.model.enums.UserRoleEnum;
import com.prodigal.system.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 角色校验
 **/
@Aspect
@Component
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
        String mustRole = permission.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        //当前登录用户
        User user = userService.getLoginUser(request);
        //TODO:有多个角色，这里需要进行匹配
        List<String> mustRoles= new ArrayList<>();
        if (mustRole.contains(",")){
            mustRoles = Arrays.stream(mustRole.split(",")).map(String::trim).collect(Collectors.toList());
        }else{
            mustRoles.add(mustRole);
        }
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRoles.get(0));
        //不需要的,放行
        if (mustRoleEnum == null){
            return joinPoint.proceed();
        }
        //验权
        //获取当前用户的角色（权限）
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(user.getUserRole());
        if (userRoleEnum == null){
            throw new BusinessException(ErrorCode.USER_NOT_PERMISSION);
        }
        //管理员
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum)){
            throw new BusinessException(ErrorCode.USER_NOT_PERMISSION);
        }
        //超级管理员
        if (UserRoleEnum.ADMINISTRATOR.equals(mustRoleEnum) && !UserRoleEnum.ADMINISTRATOR.equals(userRoleEnum)){
            throw new BusinessException(ErrorCode.USER_NOT_PERMISSION);
        }
        return joinPoint.proceed();
    }
}
