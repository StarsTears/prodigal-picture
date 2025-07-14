package com.prodigal.system.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 用户登参数
 **/
@Data
public class LoginDto implements Serializable {
    private static final long serialVersionUID = 2542971990245988594L;
    private String loginType;
    private String userAccount;
    private String userPassword;
    private String captcha;
    private String email;
}
