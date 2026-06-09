package com.prodigal.system.model.dto.email;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 编辑邮件草稿请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailUpdateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "邮件ID不能为空")
    private String id;

    private Integer type;

    /** 收件人（逗号分隔） */
    private String to;

    private String subject;

    private String txt;

    private boolean isHtml;

    private Integer status;

    private List<String> attachments;
}
