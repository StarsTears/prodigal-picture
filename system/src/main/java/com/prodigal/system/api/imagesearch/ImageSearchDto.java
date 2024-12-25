package com.prodigal.system.api.imagesearch;

import lombok.Data;

import java.io.Serializable;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 以图搜图接收参数
 **/
@Data
public class ImageSearchDto implements Serializable {
    private static final long serialVersionUID = 4282418293989654822L;
    /**
     * 图片id
     */
    private Long pictureId;
}
