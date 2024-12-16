package com.prodigal.system.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 图片编辑请求参数
 **/
@Data
public class PictureEditDto implements Serializable {
    private static final long serialVersionUID = 4636920726770266641L;
    /**
     * id
     */
    private Long id;
    /**
     * userid
     */
    private Long userId;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签（JSON 数组）
     */
    private List<String> tags;
}
