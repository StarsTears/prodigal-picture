package com.prodigal.system.model.vo;

import cn.hutool.json.JSONUtil;
import com.prodigal.system.model.entity.Email;
import com.prodigal.system.model.entity.Picture;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 邮件响应类
 **/
@Data
public class EmailVO implements Serializable {
    private static final long serialVersionUID = -4624552136157887362L;
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
     * 邮件类型
     */
    private Integer type;

    /**
     * 接收人
     */
    private String to;
    private Long receiveUserId;

    private UserVO receiveUserVO;
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
     * 封装类转对象
     * @param emailVO 封装类
     */
    public static Email voToObj(EmailVO emailVO){
        if (emailVO == null) return null;
        Email email = new Email();
        BeanUtils.copyProperties(emailVO, email);
        email.setTo(emailVO.getTo());
        email.setReceiveUserId(emailVO.getReceiveUserId());
        return email;
    }

    /**
     * 对象转封装类
     */
    public static EmailVO objToVO(Email email){
        if (email == null) return null;
        EmailVO emailVO = new EmailVO();
        BeanUtils.copyProperties(email, emailVO);
        emailVO.setTo(email.getTo());
        emailVO.setReceiveUserId(email.getReceiveUserId());
        return emailVO;
    }
}
