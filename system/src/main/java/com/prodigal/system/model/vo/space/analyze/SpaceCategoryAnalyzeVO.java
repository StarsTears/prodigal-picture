package com.prodigal.system.model.vo.space.analyze;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 空间图片分类响应类
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpaceCategoryAnalyzeVO implements Serializable {
    private static final long serialVersionUID = -2998092295034751194L;
    /**
     * 图片分类
     */
    private String category;
    /**
     * 图片数量
     */
    private Long count;
    /**
     * 分类图片大小
     */
    private Long totalSize;
}
