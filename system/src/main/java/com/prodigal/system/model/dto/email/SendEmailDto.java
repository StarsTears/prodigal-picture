package com.prodigal.system.model.dto.email;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 发送邮件请求接收类
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendEmailDto implements Serializable {
    private static final long serialVersionUID = 7999391905668452461L;
    /**
     * 邮件ID
     */
    private String id;
    /**
     * 收件人
     */
    private String to;
    /**
     * 主题
     */
    private String subject;
    /**
     * 内容
     */
    private String txt;
    /**
     * 是否包含html
     */
    private boolean isHtml;
    /**
     * 附件文件路径列表
     */
    private List<String> attachments;
}
