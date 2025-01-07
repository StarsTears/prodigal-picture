package com.prodigal.system.manager.auth.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 空间成员权限
 **/
@Data
public class SpaceUserPermission implements Serializable {
    private static final long serialVersionUID = -3425795128266385260L;
    /**
     * 权限键
     */
    private String key;
    /**
     * 权限名称
     */
    private String name;
    /**
     * 权限描述
     */
    private String description;
}
