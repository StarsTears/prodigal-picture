package com.prodigal.system.model.dto.system;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class LoginDTO implements Serializable {
    private static final long serialVersionUID = 2542971990245988594L;

    @NotBlank(message = "登录类型不能为空")
    private String loginType;

    private String userAccount;

    private String userPassword;

    private String captcha;

    @Email(message = "邮箱格式错误")
    private String email;
}
