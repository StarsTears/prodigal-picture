package com.prodigal.system.model.dto.email;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author Lang
 * @project prodigal-picture
 * @Version: 1.0
 * @description 邮箱登录请求
 * @since 2025/7/14
 */
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest implements Serializable {
    @Email(message = "邮箱格式错误")
    @NotBlank(message = "邮箱不能为空")
    private String email;
}
