package com.prodigal.system.config;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * 全局请求追踪 Filter
 * 每个请求生成/读取唯一 requestId，存入 MDC、response header、request attribute
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@WebFilter("/*")
@Component
public class RequestIdFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String requestId = request.getHeader("X-Request-Id");
        if (StrUtil.isBlank(requestId)) {
            requestId = UUID.randomUUID().toString().replace("-", "");
        }

        MDC.put("requestId", requestId);
        request.setAttribute("requestId", requestId);
        response.setHeader("X-Request-Id", requestId);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove("requestId");
        }
    }
}
