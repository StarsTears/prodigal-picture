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
    @Bean
    public Queue emailCaptchaQueue() {
        return new Queue(EmailMqConstant.CAPTCHA_QUEUE, true);
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
        factory.setErrorHandler(e -> log.warn("[send email captcha] retry exhausted, error: {}", e.getMessage(), e));
        return factory;
    }

    // ---------- 邮件发送（公告/告警）拓扑 ----------

    @Bean
    public Queue emailSendQueue() {
        return new Queue(EmailMqConstant.EMAIL_SEND_QUEUE, true);
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
        factory.setErrorHandler(e -> log.warn("[send email] retry exhausted, error: {}", e.getMessage(), e));
        return factory;
    }

    // ---------- 图片审核通知拓扑 ----------

    @Bean
    public Queue pictureReviewQueue() {
        return new Queue(PictureMqConstant.PICTURE_REVIEW_QUEUE, true);
    }

    @Bean
    public DirectExchange pictureReviewExchange() {
        return new DirectExchange(PictureMqConstant.PICTURE_REVIEW_EXCHANGE, true, false);
    }

    @Bean
    public Binding pictureReviewBinding(@Qualifier("pictureReviewQueue") Queue pictureReviewQueue,
                                        @Qualifier("pictureReviewExchange") DirectExchange pictureReviewExchange) {
        return BindingBuilder.bind(pictureReviewQueue)
                .to(pictureReviewExchange)
                .with(PictureMqConstant.PICTURE_REVIEW_ROUTING_KEY);
    }

    @Bean("pictureReviewRabbitListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory pictureReviewListenerContainerFactory(
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
        factory.setErrorHandler(e -> log.warn("[picture review notify] retry exhausted, error: {}", e.getMessage(), e));
        return factory;
    }
}
