package com.prodigal.system.manager.auth.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 空间成员-权限配置类
 **/
@Data
public class SpaceUserAuthConfig implements Serializable {
    private static final long serialVersionUID = 3608086950843426092L;
    /**
     * 权限列表
     */
    private List<SpaceUserPermission> permissions;
    /**
     * 角色列表
     */
    private List<SpaceUserRole> roles;
}
