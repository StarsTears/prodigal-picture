package com.prodigal.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.prodigal.system.model.enums.EmailTypeEnum;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 邮件信息(公告、告警···)
 **/
@Data
@Document(collection = "email")
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
     * 从···邮箱发送的信息
     * 都是从 配置的 系统邮箱 发送
     */
    private String from;
    /**
     * 邮件类型
     */
    private Integer type;

    /**
     * 接收人
     */
    private String to;
    private Long receiveUserId;

    /**
     * 状态：
     *  0:自建(草稿) 1:提交 2：已发
     */
    private Integer status;

    /**
     * 创建人
     * 关联 user 表
     */
    private Long createUserId;
    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 发送人
     */
    private Long sendUserId;

    /**
     * 发送时间
     */
    private Date sendTime;
    /**
     * 修改人
     */
    private Long updateUserId;
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
