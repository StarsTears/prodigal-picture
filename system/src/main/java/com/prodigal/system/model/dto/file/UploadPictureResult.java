package com.prodigal.system.model.dto.file;

import lombok.Data;

import java.io.Serializable;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 上传图片返回结果
 **/
@Data
public class UploadPictureResult implements Serializable {
    private static final long serialVersionUID = -9185049067244587318L;

    /**
     * 图片地址
     */
    private String originUrl;

    /**
     * 图片地址
     */
    private String url;

    /**
     * 图片地址
     */
    private String thumbnailUrl;

    /**
     * 图片源地址
     */
    private String sourceUrl;

    /**
     * 图片名称
     */
    private String picName;
    /**
     * 图片名称
     */
    private String picColor;

    /**
     * 文件体积
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private int picWidth;

    /**
     * 图片高度
     */
    private int picHeight;

    /**
     * 图片宽高比
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String picFormat;

}
