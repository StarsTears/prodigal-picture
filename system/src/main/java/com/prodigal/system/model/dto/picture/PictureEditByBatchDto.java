package com.prodigal.system.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 批量编辑接收参数类
 **/
@Data
public class PictureEditByBatchDto implements Serializable {
    private static final long serialVersionUID = -5671021064399972469L;
    /**
     * 命名规则
     */
    private String nameRule;

    /**
     * 图片id 集合
     */
    private List<Long> pictureIdList;
    /**
     * 空间 id
     */
    private Long spaceId;
    /**
     * 分类
     */
    private String category;
    /**
     * 标签
     */
    private List<String> tags;
}
