package com.prodigal.system.manager.auth;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.strategy.SaAnnotationStrategy;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @create: 2025-01-05 11:23
 * @description:
 **/
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    //注册sa-token 拦截器,打开注解式鉴权功能
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
//        WebMvcConfigurer.super.addInterceptors(registry);
        registry.addInterceptor(new SaInterceptor()).addPathPatterns("/**");
    }
    @PostConstruct
    public void rewriteSaStrategy() {
        // 重写sa-token 的注解处理器，增加注解合并功能
        SaAnnotationStrategy.instance.getAnnotation = (element,annotationClass)->{
            return AnnotatedElementUtils.getMergedAnnotation(element, annotationClass);
        };
    }
}
