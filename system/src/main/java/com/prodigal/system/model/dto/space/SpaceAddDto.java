package com.prodigal.system.model.dto.space;

import lombok.Data;

import java.io.Serializable;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 空间添加请求参数
 **/
@Data
public class SpaceAddDto implements Serializable {
    private static final long serialVersionUID = 9125766214211237093L;
    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;
}
