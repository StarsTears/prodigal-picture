package com.prodigal.system.model.dto.picture;


import lombok.Data;

import java.io.Serializable;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 图片上传请求参数
 **/
@Data
public class PictureUploadDTO implements Serializable {
    private static final long serialVersionUID = 8600896478031567126L;
    /**
     * id 用于修改
     */
    private String id;
    /**
     * 文件地址
     */
    private String fileUrl;
    /**
     * 图片名称
     */
    private String picName;
    /**
     * 空间 id
     */
    private String spaceId;
    /**
     * 请求幂等标识（UUID，防止重复提交）
     */
    private String requestId;

}
