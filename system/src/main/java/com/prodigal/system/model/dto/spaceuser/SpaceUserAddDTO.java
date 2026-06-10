package com.prodigal.system.model.dto.spaceuser;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 添加空间成员  请求参数
 **/
@Data
public class SpaceUserAddDTO implements Serializable {
    private static final long serialVersionUID = 3143948204873306567L;
    /**
     * id
     */
    private String id;

    /**
     * 空间 id
     */
    @NotNull(message = "空间ID不能为空")
    private String spaceId;

    /**
     * 用户 id
     */
    @NotNull(message = "用户ID不能为空")
    private String userId;

    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceRole;

}
