package com.prodigal.system.model.dto.user;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

@Data
public class RegisterDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "账号不能为空")
    @Size(min = 4, message = "账号长度至少 4 位")
    private String userAccount;

    private String userName;

    @NotBlank(message = "邮箱不能为空")
    private String userEmail;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度至少 6 位")
    private String userPassword;

    @NotBlank(message = "确认密码不能为空")
    @Size(min = 6, message = "确认密码长度至少 6 位")
    private String checkPassword;
}
