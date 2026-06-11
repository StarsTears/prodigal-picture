package com.prodigal.system.model.dto.user;

import com.prodigal.system.common.PageRequest;
import com.prodigal.system.model.enums.UserRoleEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 用户查询请求参数
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQueryDTO extends PageRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    private String id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    private UserRoleEnum userRole;
}
