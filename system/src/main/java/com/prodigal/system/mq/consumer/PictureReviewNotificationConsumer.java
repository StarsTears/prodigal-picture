package com.prodigal.system.mq.consumer;

import cn.hutool.core.util.StrUtil;
import com.prodigal.system.constant.CacheConstant;
import com.prodigal.system.constant.PictureMqConstant;
import com.prodigal.system.model.dto.email.EmailSendDTO;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.model.enums.EmailTypeEnum;
import com.prodigal.system.model.enums.PictureReviewStatusEnum;
import com.prodigal.system.model.message.PictureReviewedMessage;
import com.prodigal.system.service.EmailService;
import com.prodigal.system.manager.sse.SseNotificationService;
import com.prodigal.system.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 图片审核通知异步消费者——发送邮件 + SSE 通知上传者
 */
@Slf4j
@Component
public class PictureReviewNotificationConsumer {

    @Resource
    private UserService userService;

    @Resource
    private EmailService emailService;

    @Resource
    private SseNotificationService sseNotificationService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @RabbitListener(queues = PictureMqConstant.PICTURE_REVIEW_QUEUE,
            containerFactory = "pictureReviewRabbitListenerContainerFactory")
    public void onPictureReviewed(PictureReviewedMessage message) {
        String idempotentKey = CacheConstant.EMAIL_SEND_CONSUMED_PREFIX + "review:" + message.getMessageId();
        Boolean isNew = redisTemplate.opsForValue()
                .setIfAbsent(idempotentKey, "1", Duration.ofHours(24));
        if (Boolean.FALSE.equals(isNew)) {
            log.warn("图片审核通知消息已消费，跳过, messageId={}", message.getMessageId());
            return;
        }

        log.info("开始处理图片审核通知, pictureId={}, messageId={}", message.getPictureId(), message.getMessageId());

        User uploader = userService.getById(message.getUploaderUserId());
        if (uploader == null || StrUtil.isBlank(uploader.getUserEmail())) {
            log.warn("无法通知上传者，用户信息缺失: userId={}", message.getUploaderUserId());
            return;
        }

        boolean isPass = PictureReviewStatusEnum.PASS.getValue() == message.getReviewStatus();
        String picName = StrUtil.isNotBlank(message.getPicName()) ? message.getPicName() : message.getPictureId();
        String reviewMessage = StrUtil.isNotBlank(message.getReviewMessage()) ? message.getReviewMessage() : "";

        // 构建邮件
        String subject = isPass
                ? "您的图片 \"" + picName + "\" 已审核通过"
                : "您的图片 \"" + picName + "\" 未通过审核";
        String body = isPass
                ? "<p>您的图片 <b>" + picName + "</b> 已通过管理员审核，现已公开展示。</p>"
                  + (StrUtil.isNotBlank(reviewMessage) ? "<p>审核意见：" + reviewMessage + "</p>" : "")
                : "<p>您的图片 <b>" + picName + "</b> 未通过管理员审核。</p>"
                  + (StrUtil.isNotBlank(reviewMessage) ? "<p>拒绝理由：" + reviewMessage + "</p>" : "");

        EmailSendDTO emailDto = new EmailSendDTO();
        emailDto.setType(EmailTypeEnum.NOTIFY);
        emailDto.setTo(uploader.getUserEmail());
        emailDto.setSubject(subject);
        emailDto.setTxt(body);
        emailDto.setHtml(true);
        emailService.sendEmailByMimeMessage(emailDto, null);

        // SSE 通知
        sseNotificationService.notifyPictureReviewed(uploader.getId(), message.getPictureId(), isPass);

        log.info("图片审核通知处理完成, pictureId={}", message.getPictureId());
    }
}
