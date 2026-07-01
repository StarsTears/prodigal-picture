package com.prodigal.system.constant;

/**
 * 图片审核相关 RabbitMQ 拓扑
 */
public interface PictureMqConstant {

    String PICTURE_REVIEW_EXCHANGE = "picture.review.exchange";

    String PICTURE_REVIEW_QUEUE = "picture.review.queue";

    String PICTURE_REVIEW_ROUTING_KEY = "picture.review.notify";

    String PICTURE_REVIEW_DLX = "picture.review.dlx";

    String PICTURE_REVIEW_DLQ = "picture.review.dlq";
}
