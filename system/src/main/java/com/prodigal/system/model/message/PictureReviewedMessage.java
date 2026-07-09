package com.prodigal.system.model.message;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/**
 * 图片审核结果 MQ 消息体
 */
@Data
@NoArgsConstructor
public class PictureReviewedMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 消息唯一标识，用于幂等消费 */
    private String messageId;

    /** 图片 ID */
    private String pictureId;

    /** 图片名称 */
    private String picName;

    /** 上传者用户 ID */
    private String uploaderUserId;

    /** 审核状态 1=PASS 2=REJECT */
    private Integer reviewStatus;

    /** 审核意见 */
    private String reviewMessage;

    /** 缩略图 URL */
    private String url;

    /** 空间 ID */
    private String spaceId;

    public PictureReviewedMessage(String pictureId, String picName, String uploaderUserId,
                                   Integer reviewStatus, String reviewMessage, String url, String spaceId) {
        this.messageId = UUID.randomUUID().toString();
        this.pictureId = pictureId;
        this.picName = picName;
        this.uploaderUserId = uploaderUserId;
        this.reviewStatus = reviewStatus;
        this.reviewMessage = reviewMessage;
        this.url = url;
        this.spaceId = spaceId;
    }
}
