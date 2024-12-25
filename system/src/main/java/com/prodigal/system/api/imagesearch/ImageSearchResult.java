package com.prodigal.system.api.imagesearch;

import lombok.Data;

import java.io.Serializable;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 以图搜图返回结果
 **/
@Data
public class ImageSearchResult implements Serializable {
    private static final long serialVersionUID = 1853222935468386182L;

    /**
     * 来源地址
     */
    private String fromUrl;

    /**
     * 缩略图地址
     */
    private String thumbUrl;

    /**
     * 原图地址
     */
    private String objUrl;
}
