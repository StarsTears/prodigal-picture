package com.prodigal.system.model.dto.email;

import com.prodigal.system.common.PageRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description:
 **/
@Data
public class EmailQueryDto extends PageRequest implements Serializable {
    private static final long serialVersionUID = -6931914002782338908L;
    /**
     * 邮件ID
     */
    private String id;

    /**
     * 邮件类型
     */
    private Integer type;
    /**
     *  状态：0:自建(草稿) 1:提交 2：已发
     */
    private Integer status;

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
