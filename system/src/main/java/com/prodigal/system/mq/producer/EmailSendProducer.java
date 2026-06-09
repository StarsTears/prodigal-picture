package com.prodigal.system.mq.producer;

import com.prodigal.system.constant.EmailMqConstant;
import com.prodigal.system.model.message.EmailSendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 邮件发送消息生产者
 */
@Slf4j
@Component
public class EmailSendProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void send(EmailSendMessage message) {
        rabbitTemplate.convertAndSend(
                EmailMqConstant.EMAIL_SEND_EXCHANGE,
                EmailMqConstant.EMAIL_SEND_ROUTING_KEY,
                message
        );
        log.info("邮件发送消息已投递 MQ, emailId={}", message.getEmailId());
    }
}
