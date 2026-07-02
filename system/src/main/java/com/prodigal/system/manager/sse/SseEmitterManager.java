package com.prodigal.system.manager.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE 连接管理器，维护每个用户的 SseEmitter
 */
@Slf4j
@Component
public class SseEmitterManager {

    /** key: userId, value: 该用户的所有 SSE 连接(支持多 Tab) */
    private static final Map<String, Set<SseEmitter>> EMITTERS = new ConcurrentHashMap<>();

    /**
     * 创建并注册一个新的 SSE 连接
     * @param userId 用户ID
     * @param timeoutMs 超时时间（毫秒）
     */
    public SseEmitter createEmitter(String userId, long timeoutMs) {
        SseEmitter emitter = new SseEmitter(timeoutMs);
        EMITTERS.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(emitter);

        emitter.onCompletion(() -> removeFromSet(userId, emitter, "完成"));
        emitter.onTimeout(() -> removeFromSet(userId, emitter, "超时"));
        emitter.onError(e -> removeFromSet(userId, emitter, "异常"));

        Set<SseEmitter> userEmitters = EMITTERS.get(userId);
        int count = userEmitters != null ? userEmitters.size() : 0;
        log.info("SSE 连接建立, userId={}, 当前连接数={}", userId, count);
        return emitter;
    }

    /**
     * 从用户连接集合中移除指定 emitter，Set 为空时清理 key
     */
    private void removeFromSet(String userId, SseEmitter emitter, String reason) {
        Set<SseEmitter> emitters = EMITTERS.get(userId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                EMITTERS.remove(userId);
            }
        }
        log.info("SSE 连接{}, userId={}, 剩余连接数={}", reason, userId,
                emitters == null ? 0 : emitters.size());
    }

    /**
     * 向指定用户推送事件
     */
    public void sendToUser(String userId, String eventName, Object data) {
        Set<SseEmitter> emitters = EMITTERS.get(userId);
        if (emitters == null || emitters.isEmpty()) {
            log.warn("SSE 推送跳过，用户未连接, userId={}, event={}", userId, eventName);
            return;
        }
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (IOException e) {
                //单次失败只移除当前连接，不影响同用户的其他 Tab
                emitters.remove(emitter);
                log.error("SSE 推送失败, userId={}, event={}", userId, eventName, e);
            }
        }
        log.info("SSE 推送成功, userId={}, event={}, data={}", userId, eventName, data);
    }
}
