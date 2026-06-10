package com.prodigal.system.mq.consumer;

import com.prodigal.system.config.MailConfig;
import com.prodigal.system.constant.CacheConstant;
import com.prodigal.system.constant.EmailMqConstant;
import com.prodigal.system.constant.GlobalConstant;
import com.prodigal.system.exception.BusinessException;
import com.prodigal.system.exception.ErrorCode;
import com.prodigal.system.model.message.EmailCaptchaMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.time.Duration;

/**
 * 验证码邮件异步消费者
 */
@Slf4j
@Component
public class EmailCaptchaConsumer {

    @Resource
    private JavaMailSender emailSender;
    @Resource
    private MailConfig mailConfig;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @RabbitListener(queues = EmailMqConstant.CAPTCHA_QUEUE, containerFactory = "emailRabbitListenerContainerFactory")
    public void onCaptchaMessage(EmailCaptchaMessage message) {
        String idempotentKey = CacheConstant.CAPTCHA_CONSUMED_PREFIX + message.getMessageId();
        Boolean isNew = redisTemplate.opsForValue()
                .setIfAbsent(idempotentKey, "1", Duration.ofHours(24));
        if (Boolean.FALSE.equals(isNew)) {
            log.warn("验证码消息已消费，跳过, messageId={}", message.getMessageId());
            return;
        }
        log.info("开始异步发送验证码邮件, email={}, messageId={}", message.getEmail(), message.getMessageId());
        deliverVerificationEmail(message.getEmail(), message.getCode());
    }

    private void deliverVerificationEmail(String toEmail, String code) {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        try {
            helper.setFrom(mailConfig.getUsername(), GlobalConstant.PERSONAL);
            helper.setTo(toEmail);
            helper.setSubject("Prodigal Picture 登录验证码");
            helper.setText(buildCaptchaHtml(code), true);
            emailSender.send(message);
            log.info("验证码邮件发送成功, email={}", toEmail);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("验证码邮件发送失败, email={}", toEmail, e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码邮件发送失败");
        }
    }

    private String buildCaptchaHtml(String code) {
        return String.format("""
                <div style="max-width:480px;margin:0 auto;padding:32px;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;background:#f5f5f5;border-radius:8px;">
                    <div style="text-align:center;padding-bottom:24px;">
                        <h2 style="color:#1a1a1a;margin:0;">%s</h2>
                    </div>
                    <div style="background:#fff;padding:32px;border-radius:8px;text-align:center;">
                        <p style="color:#666;margin:0 0 8px;">您的登录验证码</p>
                        <div style="font-size:36px;font-weight:700;letter-spacing:6px;color:#1677ff;padding:16px 0;">%s</div>
                        <p style="color:#999;font-size:13px;margin:0;">有效期 %d 分钟，请勿泄露给他人</p>
                    </div>
                    <div style="text-align:center;padding-top:24px;">
                        <p style="color:#bbb;font-size:12px;margin:0;">此邮件由系统自动发送，请勿回复</p>
                    </div>
                </div>""", GlobalConstant.PERSONAL, code, CacheConstant.CODE_EXPIRE_MINUTES);
    }
}
