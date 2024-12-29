package com.prodigal.system.model.dto.email;

import lombok.Data;

import java.io.Serializable;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description:
 **/
@Data
public class QueryEmailDto implements Serializable {
    private static final long serialVersionUID = -6931914002782338908L;
    /**
     * 邮件ID
     */
    private String id;
    /**
     * 发件人
     */
    private String from;
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
}
