package com.prodigal.system.controller;

import com.prodigal.system.manager.sse.SseEmitterManager;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE 通知订阅
 */
@RestController
@RequestMapping("/notification")
public class NotificationController {

    private static final long SSE_TIMEOUT = 30 * 60 * 1000L;

    @Resource
    private SseEmitterManager sseEmitterManager;
    @Resource
    private UserService userService;

    @GetMapping("/subscribe")
    public SseEmitter subscribe(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return sseEmitterManager.createEmitter(loginUser.getId(), SSE_TIMEOUT);
    }
}
