package com.prodigal.system.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 单张图片查询-由于采用分表，不能只使用 pictureId 作为查询条件
 **/
@Data
public class PictureGetDto implements Serializable {
    private static final long serialVersionUID = -3064294543966651418L;
    /**
     * 图片id
     */
    private Long id;
    /**
     * 空间id
     */
    private Long spaceId;
}
