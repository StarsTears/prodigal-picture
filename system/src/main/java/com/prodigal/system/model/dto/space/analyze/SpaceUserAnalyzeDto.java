package com.prodigal.system.model.dto.space.analyze;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @create: 2024-12-29 12:37
 * @description: 用户上传行为请求参数
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class SpaceUserAnalyzeDto extends SpaceAnalyzeDto{
    private static final long serialVersionUID = -1916126268596571011L;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 时间维度 day week month year
     */
    private String timeDimension;
}
