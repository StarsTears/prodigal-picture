package com.prodigal.system.manager.auth.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 空间成员角色
 **/
@Data
public class SpaceUserRole implements Serializable {
    private static final long serialVersionUID = 2539487162340140930L;
    /**
     * 角色键
     */
    private String key;
    /**
     * 角色名称
     */
    private String name;
    /**
     * 权限键列表
     */
    private List<String> permissions;
    /**
     * 角色描述
     */
    private String description;
}
