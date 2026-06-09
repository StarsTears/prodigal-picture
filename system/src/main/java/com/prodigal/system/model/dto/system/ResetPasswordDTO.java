package com.prodigal.system.model.dto.system;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

@Data
public class ResetPasswordDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "账户不能为空！！")
    private String userAccount;

    @NotBlank(message = "邮箱不能为空！！")
    private String userEmail;

    @NotBlank(message = "验证码不能为空！！")
    private String captcha;

    @NotBlank(message = "新密码不能为空！！")
    @Size(min = 6, message = "密码长度至少为 6 位")
    private String newPassword;

    @NotBlank(message = "确认密码不能为空！！")
    @Size(min = 6, message = "密码长度至少为 6 位")
    private String checkPassword;
}
