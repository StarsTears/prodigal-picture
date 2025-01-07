package com.prodigal.system.model.dto.spaceuser;

import com.prodigal.system.common.PageRequest;
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
public class SpaceUserQueryDto extends PageRequest implements Serializable {
    private static final long serialVersionUID = 1377120394472360500L;
    /**
     * id
     */
    private Long id;

    /**
     * 空间 id
     */
    private Long spaceId;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceRole;
}
