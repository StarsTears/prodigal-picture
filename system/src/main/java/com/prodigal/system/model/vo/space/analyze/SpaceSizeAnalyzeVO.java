package com.prodigal.system.model.vo.space.analyze;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 空间大小分析响应类
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpaceSizeAnalyzeVO implements Serializable {
    private static final long serialVersionUID = 3167960926208163988L;
    /**
     * 图片大小范围
     */
    private String sizeRange;
    /**
     * 图片数量
     */
    private Long count;
}
