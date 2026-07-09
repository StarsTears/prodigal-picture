package com.prodigal.system.model.enums;

import cn.hutool.core.util.ObjUtil;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
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
    ALERT("告警",1),
    NOTIFY("通知",2),
    ;
    private final String text;
    private final int value;

    EmailTypeEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    /**
     * 根据value 获取枚举值
     */
    @JsonCreator
    public static EmailTypeEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)){
            return null;
        }
        for (EmailTypeEnum emailTypeEnum : EmailTypeEnum.values()){
            if (Objects.equals(emailTypeEnum.value, value)){
                return emailTypeEnum;
            }
        }
        throw new IllegalArgumentException("无效的邮件类型: " + value + "，有效值: [0=公告, 1=告警, 2=通知]");
    }
}
