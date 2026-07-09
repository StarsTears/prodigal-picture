package com.prodigal.system.config.rabbitmq;

import com.prodigal.system.constant.EmailMqConstant;
import com.prodigal.system.constant.PictureMqConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.retry.support.RetryTemplateBuilder;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lang
 * @project prodigal-picture
 * @Version: 1.0
 * @description 邮箱MQ配置
 * @since 2026/5/16
 */
@Slf4j
@Configuration
public class EmailRabbitMQConfig {

    // ======================== 验证码邮件拓扑 ========================

    @Bean
    public Queue emailCaptchaQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", EmailMqConstant.CAPTCHA_DLX);
        return new Queue(EmailMqConstant.CAPTCHA_QUEUE, true, false, false, args);
    }

    @Bean
    public DirectExchange emailCaptchaExchange() {
        return new DirectExchange(EmailMqConstant.CAPTCHA_EXCHANGE, true, false);
    }

    @Bean
    public Binding emailCaptchaBinding(@Qualifier("emailCaptchaQueue") Queue emailCaptchaQueue,
                                       @Qualifier("emailCaptchaExchange") DirectExchange emailCaptchaExchange) {
        return BindingBuilder.bind(emailCaptchaQueue)
                .to(emailCaptchaExchange)
                .with(EmailMqConstant.CAPTCHA_ROUTING_KEY);
    }

    @Bean
    public DirectExchange emailCaptchaDlx() {
        return new DirectExchange(EmailMqConstant.CAPTCHA_DLX, true, false);
    }

    @Bean
    public Queue emailCaptchaDlq() {
        return new Queue(EmailMqConstant.CAPTCHA_DLQ, true);
    }

    @Bean
    public Binding emailCaptchaDlqBinding(@Qualifier("emailCaptchaDlq") Queue emailCaptchaDlq,
                                          @Qualifier("emailCaptchaDlx") DirectExchange emailCaptchaDlx) {
        return BindingBuilder.bind(emailCaptchaDlq)
                .to(emailCaptchaDlx)
                .with(EmailMqConstant.CAPTCHA_ROUTING_KEY);
    }

    @Bean("emailRabbitListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        RetryTemplate retryTemplate = new RetryTemplateBuilder()
                .maxAttempts(3)
                .exponentialBackoff(Duration.ofSeconds(2), 1.5, Duration.ofSeconds(30))
                .build();
        factory.setRetryTemplate(retryTemplate);
        factory.setDefaultRequeueRejected(false);
        factory.setErrorHandler(e -> log.error("[send email captcha] retry exhausted, routing to DLQ, error: {}", e.getMessage(), e));
        return factory;
    }

    // ======================== 邮件发送（公告/告警）拓扑 ========================

    @Bean
    public Queue emailSendQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", EmailMqConstant.EMAIL_SEND_DLX);
        return new Queue(EmailMqConstant.EMAIL_SEND_QUEUE, true, false, false, args);
    }

    @Bean
    public DirectExchange emailSendExchange() {
        return new DirectExchange(EmailMqConstant.EMAIL_SEND_EXCHANGE, true, false);
    }

    @Bean
    public Binding emailSendBinding(@Qualifier("emailSendQueue") Queue emailSendQueue,
                                    @Qualifier("emailSendExchange") DirectExchange emailSendExchange) {
        return BindingBuilder.bind(emailSendQueue)
                .to(emailSendExchange)
                .with(EmailMqConstant.EMAIL_SEND_ROUTING_KEY);
    }

    @Bean
    public DirectExchange emailSendDlx() {
        return new DirectExchange(EmailMqConstant.EMAIL_SEND_DLX, true, false);
    }

    @Bean
    public Queue emailSendDlq() {
        return new Queue(EmailMqConstant.EMAIL_SEND_DLQ, true);
    }

    @Bean
    public Binding emailSendDlqBinding(@Qualifier("emailSendDlq") Queue emailSendDlq,
                                       @Qualifier("emailSendDlx") DirectExchange emailSendDlx) {
        return BindingBuilder.bind(emailSendDlq)
                .to(emailSendDlx)
                .with(EmailMqConstant.EMAIL_SEND_ROUTING_KEY);
    }

    @Bean("emailSendRabbitListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory emailSendListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        RetryTemplate retryTemplate = new RetryTemplateBuilder()
                .maxAttempts(3)
                .exponentialBackoff(Duration.ofSeconds(2), 1.5, Duration.ofSeconds(30))
                .build();
        factory.setRetryTemplate(retryTemplate);
        factory.setDefaultRequeueRejected(false);
        factory.setErrorHandler(e -> log.error("[send email] retry exhausted, routing to DLQ, error: {}", e.getMessage(), e));
        return factory;
    }

}
