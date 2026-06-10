package com.prodigal.system.model.dto.picture;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 批量编辑接收参数类
 **/
@Data
public class PictureEditByBatchDTO implements Serializable {
    private static final long serialVersionUID = -5671021064399972469L;
    /**
     * 命名规则
     */
    private String nameRule;

    /**
     * 图片id 集合
     */
    @NotEmpty(message = "图片ID列表不能为空")
    private List<String> pictureIdList;
    /**
     * 空间 id
     */
    @NotNull(message = "空间ID不能为空")
    private String spaceId;
    /**
     * 分类
     */
    private String category;
    /**
     * 标签
     */
    private List<String> tags;
}
