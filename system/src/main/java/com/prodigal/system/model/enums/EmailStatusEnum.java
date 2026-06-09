package com.prodigal.system.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

import java.util.Objects;

/**
 * 邮件状态枚举
 */
@Getter
public enum EmailStatusEnum {
    DRAFT("草稿", 0),
    SUBMITTED("已提交", 1),
    SENT("已发", 2),
    ;
    private final String text;
    private final int value;

    EmailStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举值
     */
    public static EmailStatusEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (EmailStatusEnum statusEnum : EmailStatusEnum.values()) {
            if (Objects.equals(statusEnum.value, value)) {
                return statusEnum;
            }
        }
        return null;
    }
}
