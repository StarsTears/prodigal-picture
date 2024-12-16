package com.prodigal.system.model.dto.picture;

import com.prodigal.system.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @create: 2024-12-12 17:41
 * @description: 图片查询请求参数
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class PictureQueryDto extends PageRequest implements Serializable {
    private static final long serialVersionUID = -642707868316141502L;

    /**
     * id
     */
    private Long id;

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
     * 标签
     */
    private List<String> tags;

    /**
     * 文件体积
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private Integer picWidth;

    /**
     * 图片高度
     */
    private Integer picHeight;

    /**
     * 图片比例
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String picFormat;

    /**
     * 搜索词（同时搜名称、简介等）
     */
    private String searchText;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 审核状态
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;

    /**
     * 审核人 id
     */
    private Long reviewerId;
}



