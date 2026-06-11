package com.prodigal.system.manager.sse;

import com.prodigal.system.constant.SseEventConstant;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * SSE 通知服务实现
 */
@Slf4j
@Component
public class SseNotificationService {

    @Resource
    private SseEmitterManager sseEmitterManager;

    public void notifyEmailSent(String userId) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("type", SseEventConstant.EMAIL_SENT);
            data.put("message", "您收到一封新的通知邮件");
            sseEmitterManager.sendToUser(userId, SseEventConstant.EMAIL_SENT, data);
        } catch (Exception e) {
            log.error("SSE 通知收件人失败, userId={}", userId, e);
        }
    }

    public void notifyEmailSendSuccess(String userId, String emailId) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("type", SseEventConstant.EMAIL_SEND_SUCCESS);
            data.put("message", "邮件发送成功");
            data.put("emailId", emailId);
            sseEmitterManager.sendToUser(userId, SseEventConstant.EMAIL_SEND_SUCCESS, data);
        } catch (Exception e) {
            log.error("SSE 通知发送者失败, userId={}, emailId={}", userId, emailId, e);
        }
    }

    public void notifyPictureReviewed(String userId, String pictureId, boolean isPass) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("type", SseEventConstant.PICTURE_REVIEWED);
            data.put("message", isPass ? "图片审核已通过" : "图片审核未通过");
            data.put("pictureId", pictureId);
            data.put("reviewStatus", isPass ? 1 : 2);
            sseEmitterManager.sendToUser(userId, SseEventConstant.PICTURE_REVIEWED, data);
        } catch (Exception e) {
            log.error("SSE 通知审核结果失败, userId={}, pictureId={}", userId, pictureId, e);
        }
    }
}
