package com.prodigal.system.model.dto.picture;

import com.prodigal.system.api.aliyunai.model.dto.CreateOutPaintingTaskDto;
import lombok.Data;

import java.io.Serializable;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: AI扩图请求参数接收类
 **/
@Data
public class CreatePictureOutPaintingTaskDto implements Serializable {
    private static final long serialVersionUID = -4997214864240922424L;
    /**
     * 图片ID
     */
    private Long pictureId;
    /**
     * 扩图参数
     */
    private CreateOutPaintingTaskDto.Parameters parameters;
}
