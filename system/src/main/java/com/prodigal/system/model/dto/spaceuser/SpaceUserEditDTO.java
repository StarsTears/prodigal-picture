package com.prodigal.system.model.dto.spaceuser;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 编辑 空间成员 请求参数
 **/
@Data
public class SpaceUserEditDTO implements Serializable {
    private static final long serialVersionUID = 5236456629496783013L;
    /**
     * id
     */
    @NotNull(message = "id不能为空")
    private String id;

    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceRole;
}
