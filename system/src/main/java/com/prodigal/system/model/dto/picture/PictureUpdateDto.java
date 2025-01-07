package com.prodigal.system.model.dto.picture;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 图片更新请求参数
 **/
@Data
public class PictureUpdateDto implements Serializable {
    private static final long serialVersionUID = -3699356402717064433L;
    /**
     * id
     */
    private Long id;
    /**
     * spaceID
     */
    private Long spaceId;
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
