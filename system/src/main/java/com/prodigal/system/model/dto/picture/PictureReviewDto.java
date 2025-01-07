package com.prodigal.system.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 接收图片审核参数类
 **/
@Data
public class PictureReviewDto implements Serializable {
    private static final long serialVersionUID = -3733921801816510312L;
    /**
     * id
     */
    private Long id;
    /**
     * 空间id
     */
    private Long spaceId;
    /**
     * 审核状态
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;

}
