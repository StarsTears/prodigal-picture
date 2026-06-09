package com.prodigal.system.manager.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE 连接管理器，维护每个用户的 SseEmitter
 */
@Slf4j
@Component
public class SseEmitterManager {

    /** key: userId, value: 该用户的 SSE 连接 */
    private static final Map<Long, SseEmitter> EMITTERS = new ConcurrentHashMap<>();

    /**
     * 创建并注册一个新的 SSE 连接
     * @param userId 用户ID
     * @param timeoutMs 超时时间（毫秒）
     */
    public SseEmitter createEmitter(Long userId, long timeoutMs) {
        SseEmitter emitter = new SseEmitter(timeoutMs);
        EMITTERS.put(userId, emitter);

        emitter.onCompletion(() -> {
            EMITTERS.remove(userId);
            log.info("SSE 连接完成, userId={}", userId);
        });
        emitter.onTimeout(() -> {
            EMITTERS.remove(userId);
            log.info("SSE 连接超时, userId={}", userId);
        });
        emitter.onError(e -> {
            EMITTERS.remove(userId);
            log.info("SSE 连接异常, userId={}", userId);
        });

        log.info("SSE 连接建立, userId={}", userId);
        return emitter;
    }

    /**
     * 向指定用户推送事件
     */
    public void sendToUser(Long userId, String eventName, Object data) {
        SseEmitter emitter = EMITTERS.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (IOException e) {
                EMITTERS.remove(userId);
                log.error("SSE 推送失败, userId={}", userId, e);
            }
        }
    }

    /**
     * 移除用户的连接
     */
    public void removeEmitter(Long userId) {
        SseEmitter emitter = EMITTERS.remove(userId);
        if (emitter != null) {
            emitter.complete();
        }
    }
}
