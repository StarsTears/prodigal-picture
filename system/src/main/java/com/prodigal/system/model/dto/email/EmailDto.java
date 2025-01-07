package com.prodigal.system.model.dto.email;

import com.prodigal.system.model.enums.EmailTypeEnum;
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
public class EmailDto implements Serializable {
    private static final long serialVersionUID = 7999391905668452461L;
    /**
     * 邮件ID
     */
    private String id;

    /**
     * 邮件类型
     */
    private Integer type;
    /**
     * 状态：
     *  0:自建(草稿) 1:提交 2：已发
     */
    private Integer status;

    /**
     * 收件人
     */
    private String to;
    private Long receiveUserId;
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
