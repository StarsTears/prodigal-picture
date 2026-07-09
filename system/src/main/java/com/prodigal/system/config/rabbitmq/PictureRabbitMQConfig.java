package com.prodigal.system.config.rabbitmq;

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
 * @description 图片MQ配置
 * @since 2026/7/1
 */
@Slf4j
@Configuration
public class PictureRabbitMQConfig {

    // ======================== 图片审核通知拓扑 ========================
    @Bean
    public Queue pictureReviewQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", PictureMqConstant.PICTURE_REVIEW_DLX);
        return new Queue(PictureMqConstant.PICTURE_REVIEW_QUEUE, true, false, false, args);
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

    @Bean
    public DirectExchange pictureReviewDlx() {
        return new DirectExchange(PictureMqConstant.PICTURE_REVIEW_DLX, true, false);
    }

    @Bean
    public Queue pictureReviewDlq() {
        return new Queue(PictureMqConstant.PICTURE_REVIEW_DLQ, true);
    }

    @Bean
    public Binding pictureReviewDlqBinding(@Qualifier("pictureReviewDlq") Queue pictureReviewDlq,
                                           @Qualifier("pictureReviewDlx") DirectExchange pictureReviewDlx) {
        return BindingBuilder.bind(pictureReviewDlq)
                .to(pictureReviewDlx)
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
        factory.setDefaultRequeueRejected(false);
        factory.setErrorHandler(e -> log.error("[picture review notify] retry exhausted, routing to DLQ, error: {}", e.getMessage(), e));
        return factory;
    }
}
