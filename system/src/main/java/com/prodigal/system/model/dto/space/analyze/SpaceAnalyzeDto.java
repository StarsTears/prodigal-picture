package com.prodigal.system.model.dto.space.analyze;

import lombok.Data;

import java.io.Serializable;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 图库分析公共请求接收类
 **/
@Data
public class SpaceAnalyzeDto implements Serializable {
    private static final long serialVersionUID = -3500171045451241034L;
    /**
     * 空间ID
     */
    private Long spaceId;
    /**
     * 是否查询公共空间
     */
    private boolean queryPublic;
    /**
     * 是否查询所有空间
     */
    private boolean queryAll;

}
