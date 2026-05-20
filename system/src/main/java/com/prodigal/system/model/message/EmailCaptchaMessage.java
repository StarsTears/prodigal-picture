package com.prodigal.system.model.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 验证码邮件 MQ 消息体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailCaptchaMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String email;

    private String code;
}
