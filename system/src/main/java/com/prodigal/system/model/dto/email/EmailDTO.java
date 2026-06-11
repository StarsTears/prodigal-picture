package com.prodigal.system.model.dto.email;

import com.prodigal.system.model.enums.EmailStatusEnum;
import com.prodigal.system.model.enums.EmailTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class EmailDTO implements Serializable {
    private static final long serialVersionUID = 7999391905668452461L;
    /**
     * 邮件ID
     */
    private String id;

    /**
     * 邮件类型
     */
    @NotNull(message = "邮件类型不能为空")
    private EmailTypeEnum type;
    /**
     * 状态：
     *  0:草稿 1:发送中 2:已发
     */
    @NotNull(message = "邮件状态不能为空")
    private EmailStatusEnum status;

    /**
     * 收件人
     */
    private String to;
    private String receiveUserId;
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
