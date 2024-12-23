package com.prodigal.system.model.dto.space;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 空间更新请求参数
 **/
@Data
public class SpaceUpdateDto implements Serializable {
    private static final long serialVersionUID = -7265087907677433764L;
    /**
     * id
     */
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;

    /**
     * 空间图片的最大总大小
     */
    private Long maxSize;

    /**
     * 空间图片的最大数量
     */
    private Long maxCount;
}
