package com.prodigal.system.model.dto.user;

import com.prodigal.system.model.enums.UserRoleEnum;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 用户更新请求参数
 **/
@Data
public class UserUpdateDTO implements Serializable {
    private static final long serialVersionUID = -8254610615856656017L;
    /**
     * id
     */
    @NotNull(message = "id不能为空")
    private String id;

    /**
     * 账号
     */
    @NotBlank(message = "账号不能为空")
    private String userAccount;

    /**
     * 用户邮箱
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式错误")
    private String userEmail;
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
