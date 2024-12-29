package com.prodigal.system.model.dto.space.analyze;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @create: 2024-12-29 12:50
 * @description: 空间使用排名请求参数
 **/
@Data
public class SpaceRankAnalyzeDto implements Serializable {
    private static final long serialVersionUID = 6560717111168006407L;
    /**
     * 排名前 N 的空间
     */
    private Integer topN=10;
}
