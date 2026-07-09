package com.prodigal.system.mq.consumer;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.prodigal.system.config.MailConfig;
import com.prodigal.system.constant.CacheConstant;
import com.prodigal.system.constant.EmailMqConstant;
import com.prodigal.system.constant.GlobalConstant;
import com.prodigal.system.manager.sse.SseNotificationService;
import com.prodigal.system.model.entity.Email;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.model.enums.EmailStatusEnum;
import com.prodigal.system.model.enums.EmailTypeEnum;
import com.prodigal.system.model.message.EmailSendMessage;
import com.prodigal.system.service.UserService;
import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.util.Date;
import java.util.List;

/**
 * 邮件发送异步消费者（公告/告警）
 */
@Slf4j
@Component
public class EmailSendConsumer {

    @Resource
    private JavaMailSender emailSender;
    @Resource
    private MailConfig mailConfig;
    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private UserService userService;
    @Resource
    private SseNotificationService sseNotificationService;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @RabbitListener(queues = EmailMqConstant.EMAIL_SEND_QUEUE, containerFactory = "emailSendRabbitListenerContainerFactory")
    public void onSendMessage(EmailSendMessage message) {
        String idempotentKey = CacheConstant.EMAIL_SEND_CONSUMED_PREFIX + message.getMessageId();
        Boolean isNew = redisTemplate.opsForValue()
                .setIfAbsent(idempotentKey, "1", Duration.ofHours(24));
        if (Boolean.FALSE.equals(isNew)) {
            log.warn("邮件发送消息已消费，跳过, messageId={}", message.getMessageId());
            return;
        }

        String emailId = message.getEmailId();
        log.info("开始异步发送邮件, emailId={}, messageId={}", emailId, message.getMessageId());

        Email email = mongoTemplate.findById(emailId, Email.class);
        if (email == null) {
            log.error("邮件记录不存在, emailId={}", emailId);
            return;
        }

        // 幂等判断：已发送则跳过
        if (Integer.valueOf(EmailStatusEnum.SENT.getValue()).equals(email.getStatus())) {
            log.warn("邮件已发送，跳过重复消费, emailId={}", emailId);
            return;
        }

        MimeMessage mimeMessage = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);

        try {
            helper.setFrom(mailConfig.getUsername(), GlobalConstant.PERSONAL);

            // 公告类型：若未指定收件人，则查询全量用户邮箱
            EmailTypeEnum typeEnum = EmailTypeEnum.getEnumByValue(email.getType());
            if (EmailTypeEnum.NOTICE.equals(typeEnum)) {
                if (StrUtil.isNotBlank(email.getTo())) {
                    helper.setTo(email.getTo().split(","));
                } else {
                    List<String> emailList = userService.getBaseMapper()
                            .selectObjs(new QueryWrapper<User>()
                                    .select("user_email")
                                    .eq("is_delete", 0))
                            .stream()
                            .map(obj -> (String) obj)
                            .toList();
                    if (emailList.isEmpty()) {
                        log.warn("没有可发送的用户邮箱, emailId={}", emailId);
                        return;
                    }
                    helper.setTo(emailList.toArray(new String[0]));
                }
            } else {
                if (StrUtil.isBlank(email.getTo())) {
                    log.error("邮件收件人为空, emailId={}", emailId);
                    return;
                }
                helper.setTo(email.getTo().split(","));
            }

            helper.setSubject(email.getSubject());
            helper.setText(email.getTxt(), email.isHtml());

            emailSender.send(mimeMessage);
            log.info("邮件发送成功, emailId={}", emailId);

            // 更新状态为已发
            email.setStatus(EmailStatusEnum.SENT.getValue());
            email.setSendTime(new Date());
            mongoTemplate.save(email);

            // SSE 通知收件人刷新
            notifyRecipients(email);
            // SSE 通知发送者
            notifySender(email);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("邮件发送失败, emailId={}", emailId, e);
            throw new RuntimeException("邮件发送失败: " + e.getMessage(), e);
        }
    }

    /**
     * SSE 通知收件人刷新界面
     */
    private void notifyRecipients(Email email) {
        if (StrUtil.isBlank(email.getTo())) {
            log.info("SSE notifyRecipients 跳过，收件人为空, emailId={}", email.getId());
            return;
        }
        String[] emails = email.getTo().split(",");
        log.info("SSE 开始通知收件人, emailId={}, 收件人数={}", email.getId(), emails.length);
        for (String recipientEmail : emails) {
            String trimmed = recipientEmail.trim();
            if (StrUtil.isBlank(trimmed)) {
                continue;
            }
            User recipient = userService.lambdaQuery()
                    .eq(User::getUserEmail, trimmed)
                    .eq(User::getIsDelete, 0)
                    .one();
            if (recipient != null) {
                sseNotificationService.notifyEmailSent(recipient.getId());
            } else {
                log.warn("收件人不存在于用户表, email={}", trimmed);
            }
        }
    }

    /**
     * SSE 通知发送者邮件已发送成功
     */
    private void notifySender(Email email) {
        if (StrUtil.isBlank(email.getSendUserId())) {
            log.info("SSE notifySender 跳过，sendUserId 为空, emailId={}", email.getId());
            return;
        }
        sseNotificationService.notifyEmailSendSuccess(email.getSendUserId(), email.getId());
    }
}
