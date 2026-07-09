package com.prodigal.system.model.message;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/**
 * 邮件发送 MQ 消息体
 */
@Data
@NoArgsConstructor
public class EmailSendMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息唯一标识，用于幂等消费
     */
    private String messageId;

    private String emailId;

    public EmailSendMessage(String emailId) {
        this.messageId = UUID.randomUUID().toString();
        this.emailId = emailId;
    }
}
