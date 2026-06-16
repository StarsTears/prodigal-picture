package com.prodigal.system.config;

import cn.hutool.core.util.StrUtil;
import com.prodigal.system.common.BaseResult;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 在所有 BaseResult 响应体中自动注入 requestId
 */
@RestControllerAdvice
public class RequestIdBodyAdvice implements ResponseBodyAdvice<BaseResult<?>> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return BaseResult.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public BaseResult<?> beforeBodyWrite(BaseResult<?> body, MethodParameter returnType,
            MediaType contentType, Class<? extends HttpMessageConverter<?>> converterType,
            ServerHttpRequest request, ServerHttpResponse response) {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null && body != null) {
            String requestId = (String) attrs.getRequest().getAttribute("requestId");
            if (StrUtil.isNotBlank(requestId)) {
                body.setRequestId(requestId);
            }
        }
        return body;
    }
}
