package com.prodigal.system.mq.producer;

import cn.hutool.core.util.StrUtil;
import com.prodigal.system.constant.EmailMqConstant;
import com.prodigal.system.constant.PictureMqConstant;
import com.prodigal.system.model.message.EmailCaptchaMessage;
import com.prodigal.system.model.message.EmailSendMessage;
import com.prodigal.system.model.message.PictureReviewedMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 邮件消息生成者
 */
@Slf4j
@Component
public class EmailProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void publishCaptcha(EmailCaptchaMessage message) {
        String messageId = message.getMessageId();
        if (StrUtil.isBlank(messageId)) {
            messageId = UUID.randomUUID().toString();
            message.setMessageId(messageId);
        }
        rabbitTemplate.convertAndSend(
                EmailMqConstant.CAPTCHA_EXCHANGE,
                EmailMqConstant.CAPTCHA_ROUTING_KEY,
                message
        );
        log.info("验证码邮件已投递 MQ, email={},messageId={}", message.getEmail(), messageId);
    }

    public void publishNotify(EmailSendMessage message) {
        String messageId = message.getMessageId();
        if (StrUtil.isBlank(messageId)) {
            messageId = UUID.randomUUID().toString();
            message.setMessageId(messageId);
        }
        rabbitTemplate.convertAndSend(
                EmailMqConstant.EMAIL_SEND_EXCHANGE,
                EmailMqConstant.EMAIL_SEND_ROUTING_KEY,
                message
        );
        log.info("邮件发送消息已投递 MQ, emailId={},messageId={}", message.getEmailId(), messageId);
    }

    public void publishPictureReviewed(PictureReviewedMessage message) {
        String messageId = message.getMessageId();
        if (StrUtil.isBlank(messageId)) {
            messageId = UUID.randomUUID().toString();
            message.setMessageId(messageId);
        }
        rabbitTemplate.convertAndSend(
                PictureMqConstant.PICTURE_REVIEW_EXCHANGE,
                PictureMqConstant.PICTURE_REVIEW_ROUTING_KEY,
                message
        );
        log.info("图片审核通知已投递 MQ, pictureId={}, messageId={}", message.getPictureId(), messageId);
    }
}
