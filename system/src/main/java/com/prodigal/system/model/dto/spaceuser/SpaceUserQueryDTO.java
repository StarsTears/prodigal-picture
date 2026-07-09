package com.prodigal.system.model.dto.spaceuser;

import com.prodigal.system.common.PageRequest;
import com.prodigal.system.model.enums.SpaceRoleEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 查询 空间成员 请求参数
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class SpaceUserQueryDTO extends PageRequest implements Serializable {
    private static final long serialVersionUID = 1377120394472360500L;
    /**
     * id
     */
    private String id;

    /**
     * 空间 id
     */
    private String spaceId;

    /**
     * 用户 id
     */
    private String userId;

    /**
     * 空间角色：viewer/editor/admin
     */
    private SpaceRoleEnum spaceRole;
}
