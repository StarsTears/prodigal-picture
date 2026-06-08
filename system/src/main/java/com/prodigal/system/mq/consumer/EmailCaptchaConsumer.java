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
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;

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

    @RabbitListener(queues = EmailMqConstant.CAPTCHA_QUEUE,containerFactory = "rabbitListenerContainerFactory")
    public void onCaptchaMessage(EmailCaptchaMessage message) {
        log.info("开始异步发送验证码邮件, email={}", message.getEmail());
        deliverVerificationEmail(message.getEmail(), message.getCode());
    }

    private void deliverVerificationEmail(String toEmail, String code) {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        try {
            helper.setFrom(mailConfig.getUsername(), GlobalConstant.PERSONAL);
            helper.setTo(toEmail);
            helper.setSubject("Prodigal Picture 登录验证码");
            helper.setText("您的验证码是：" + code + "\n\n有效期 "
                    + CacheConstant.CODE_EXPIRE_MINUTES + " 分钟，请勿泄露给他人。");
            emailSender.send(message);
            log.info("验证码邮件发送成功, email={}", toEmail);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("验证码邮件发送失败, email={}", toEmail, e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码邮件发送失败");
        }
    }
}
