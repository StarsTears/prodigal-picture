package com.prodigal.system.model.vo.space.analyze;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 空间图片标签分析响应类
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpaceTagAnalyzeVO implements Serializable {
    private static final long serialVersionUID = -7701930541099256418L;
    /**
     * 标签
     */
    private String tag;
    /**
     * 数量
     */
    private Long count;

}
