package com.prodigal.system.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 批量抓取图片请求参数
 **/
@Data
public class PictureUploadByBatchDto implements Serializable {
    private static final long serialVersionUID = 7990177905867806645L;
    /**
     * 图片抓取源
     */
    private String url;
    /**
     * 移量
     */
    private Integer offset; // 添加偏移量参数
    /**
     * 关键词
     */
    private String searchText;
    /**
     * 抓取条数
     */
    private Integer count = 10;
    /**
     * 图片名称前缀
     */
    private String namePrefix;
    /**
     * 分类
     */
    private String category;
    /**
     * 标签
     */
    private String tags;
}
