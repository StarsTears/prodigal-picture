package com.prodigal.system.model.dto.email;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 新增邮件草稿请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailAddDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "邮件类型不能为空")
    private Integer type;

    /** 收件人（逗号分隔） */
    private String to;

    private String subject;

    private String txt;

    private boolean isHtml;

    private List<String> attachments;
}
