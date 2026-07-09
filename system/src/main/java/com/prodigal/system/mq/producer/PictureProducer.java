package com.prodigal.system.mq.producer;

import cn.hutool.core.util.StrUtil;
import com.prodigal.system.constant.PictureMqConstant;
import com.prodigal.system.model.message.PictureReviewedMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @author Lang
 * @project prodigal-picture
 * @Version: 1.0
 * @description 图片推送
 * @since 2026/7/1
 */
@Slf4j
@Component
public class PictureProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;

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
