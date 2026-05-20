package com.prodigal.system.mq.producer;

import com.prodigal.system.constant.EmailMqConstant;
import com.prodigal.system.model.message.EmailCaptchaMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 验证码邮件消息生产者
 */
@Slf4j
@Component
public class EmailCaptchaProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void send(EmailCaptchaMessage message) {
        rabbitTemplate.convertAndSend(
                EmailMqConstant.CAPTCHA_EXCHANGE,
                EmailMqConstant.CAPTCHA_ROUTING_KEY,
                message
        );
        log.info("验证码邮件已投递 MQ, email={}", message.getEmail());
    }
}
