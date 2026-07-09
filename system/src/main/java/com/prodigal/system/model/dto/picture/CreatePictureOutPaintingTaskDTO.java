package com.prodigal.system.model.dto.picture;

import com.prodigal.system.api.aliyunai.model.dto.CreateOutPaintingTaskDTO;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: AI扩图请求参数接收类
 **/
@Data
public class CreatePictureOutPaintingTaskDTO implements Serializable {
    private static final long serialVersionUID = -4997214864240922424L;
    /**
     * 图片ID
     */
    @NotNull(message = "图片ID不能为空")
    private String pictureId;
    /**
     * 扩图参数
     */
    private CreateOutPaintingTaskDTO.Parameters parameters;
}
