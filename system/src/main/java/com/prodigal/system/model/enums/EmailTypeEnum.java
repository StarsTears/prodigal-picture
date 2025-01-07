package com.prodigal.system.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

import java.util.Objects;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 邮件类型枚举
 **/
@Getter
public enum EmailTypeEnum {
    NOTICE("公告",0),
    alert("告警",1),
    ;
    private final String text;
    private final int value;

    EmailTypeEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据value 获取枚举值
     */
    public static EmailTypeEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)){
            return null;
        }
        for (EmailTypeEnum emailTypeEnum : EmailTypeEnum.values()){
            if (Objects.equals(emailTypeEnum.value, value)){
                return emailTypeEnum;
            }
        }
        return null;
    }
}
