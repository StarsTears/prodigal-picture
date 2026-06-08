package com.prodigal.system.model.dto.picture;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 图片编辑请求参数
 **/
@Data
public class PictureEditDTO implements Serializable {
    private static final long serialVersionUID = 4636920726770266641L;
    /**
     * id
     */
    @NotNull(message = "id不能为空")
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
    @Size(max = 1000, message = "简介最长1000字符")
    private String introduction;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签（JSON 数组）
     */
    private List<String> tags;
    /**
     * 空间 id
     */
    private Long spaceId;
}
