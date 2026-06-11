package com.prodigal.system.model.enums;

import cn.hutool.core.util.ObjUtil;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Objects;

/**
 * 邮件状态枚举
 */
@Getter
public enum EmailStatusEnum {
    DRAFT("草稿", 0),
    SUBMITTED("发送中", 1),
    SENT("已发", 2),
    ;
    private final String text;
    private final int value;

    EmailStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    /**
     * 根据 value 获取枚举值
     */
    @JsonCreator
    public static EmailStatusEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (EmailStatusEnum statusEnum : EmailStatusEnum.values()) {
            if (Objects.equals(statusEnum.value, value)) {
                return statusEnum;
            }
        }
        throw new IllegalArgumentException("无效的邮件状态: " + value + "，有效值: [0=草稿, 1=发送中, 2=已发]");
    }
}
