package com.prodigal.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 邮件信息
 **/
@Data
@Document(collection = "message")
public class Email implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 5595215741557819745L;

    @Id
    private String id;
    /**
     * 主题
     */
    private String subject;
    /**
     * 邮件内容
     */
    private String txt;
    /**
     * 是否为Html
     */
    private boolean isHtml;
    /**
     * 附件
     */
    private String attachments;
    /**
     * 发送时间
     */
    private Date sendTime;
    /**
     * 发送人
     */
    private Long createUserId;
    /**
     * 从···邮箱发送的信息
     */
    private String from;
    /**
     * 接收时间
     */
    private Date receiveDate;
    /**
     * 接收人
     */
    private Long receiveUserId;
    private String to;

    /*状态：0:自建(草稿) 1:已发*/
    private Integer status;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;
}
