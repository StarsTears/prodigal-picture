package com.prodigal.system.model.vo.space.analyze;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 用户上传行为分析响应类
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpaceUserAnalyzeVO implements Serializable {
    private static final long serialVersionUID = -3177615699002664945L;
    /**
     * 时间区间
     */
    private String timeRange;
    /**
     * 用户上传数量
     */
    private Long count;
}
