package com.prodigal.system.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 用户登参数
 **/
@Data
public class LoginDto implements Serializable {
    private String userAccount;
    private String userPassword;
}
