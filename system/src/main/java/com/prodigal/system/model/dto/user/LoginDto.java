package com.prodigal.system.model.dto.user;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
public class LoginDto implements Serializable {
    private static final long serialVersionUID = 2542971990245988594L;

    @NotBlank(message = "登录类型不能为空")
    private String loginType;

    private String userAccount;

    private String userPassword;

    private String captcha;

    private String email;
}
