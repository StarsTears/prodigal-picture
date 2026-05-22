package com.prodigal.system.config.rabbitmq;

import com.prodigal.system.constant.EmailMqConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.MessageConverter;
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
    public Binding emailCaptchaBinding(Queue emailCaptchaQueue, DirectExchange emailCaptchaExchange) {
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
//        RetryTemplate retryTemplate = new RetryTemplateBuilder()
//                .maxAttempts(3)
//                .exponentialBackoff(Duration.ofSeconds(2), 1.5, Duration.ofSeconds(30))
//                .build();
//        factory.setRetryTemplate(retryTemplate);
        factory.setErrorHandler(e -> log.warn("[send email captcha] Error: {}", e.getMessage(), e));
        return factory;
    }
}
