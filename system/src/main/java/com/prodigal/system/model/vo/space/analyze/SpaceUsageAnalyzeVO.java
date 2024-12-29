package com.prodigal.system.model.vo.space.analyze;

import lombok.Data;

import java.io.Serializable;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 空间资源使用响应类
 **/
@Data
public class SpaceUsageAnalyzeVO implements Serializable {
    private static final long serialVersionUID = 9158646350876664317L;
    /**
     * 已使用大小
     */
    private Long usedSize;
    /**
     * 总大小
     */
    private Long maxSize;
    /**
     * 空间使用比例
     */
    private Double sizeUsageRatio;
    /**
     * 当前图片数量
     */
    private Long usedCount;
    /**
     * 最大图片数量
     */
    private Long maxCount;
    /**
     * 图片数量占比
     */
    private Double countUsageRatio;
}
