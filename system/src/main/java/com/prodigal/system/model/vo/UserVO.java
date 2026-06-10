package com.prodigal.system.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 用户信息脱敏
 **/
@Data
public class UserVO implements Serializable {
    /**
     * id
     */
    private String id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户昵称
     */
    private String userEmail;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    private String userRole;
    /**
     * 会员编码
     */
    private Long vipNumber;

    /**
     * 邀请用户ID
     */
    private String inviteUser;

    /**
     * 分享码
     */
    private String shareCode;

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
}
