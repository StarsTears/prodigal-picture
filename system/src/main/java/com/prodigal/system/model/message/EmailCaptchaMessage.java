package com.prodigal.system.model.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/**
 * 验证码邮件 MQ 消息体
 */
@Data
@NoArgsConstructor
public class EmailCaptchaMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息唯一标识，用于幂等消费
     */
    private String messageId;

    private String email;

    private String code;

    public EmailCaptchaMessage(String email, String code) {
        this.messageId = UUID.randomUUID().toString();
        this.email = email;
        this.code = code;
    }
}
