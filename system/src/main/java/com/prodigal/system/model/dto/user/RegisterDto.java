package com.prodigal.system.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 用户注册请求参数
 **/
@Data
public class RegisterDto implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 账号
     */
    private String userAccount;
    /**
     * 密码
     */
    private String userPassword;
    /**
     * 确认密码
     */
    private String checkPassword;
}
