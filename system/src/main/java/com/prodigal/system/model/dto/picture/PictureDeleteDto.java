package com.prodigal.system.model.dto.picture;

import com.prodigal.system.common.DeleteRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 图片删除请求参数
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class PictureDeleteDto extends DeleteRequest {
    /**
     * 空间id
     */
    private Long spaceId;
}
