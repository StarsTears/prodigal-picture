package com.prodigal.system.model.enums;

import cn.hutool.core.util.ObjUtil;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 空间类型枚举
 **/
@Getter
public enum SpaceTypeEnum {

    PRIVATE("私有空间", 0),
    TEAM("团队空间", 1);

    private final String text;

    private final int value;

    SpaceTypeEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    /**
     * 根据 value 获取枚举
     */
    @JsonCreator
    public static SpaceTypeEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (SpaceTypeEnum spaceTypeEnum : SpaceTypeEnum.values()) {
            if (spaceTypeEnum.value == value) {
                return spaceTypeEnum;
            }
        }
        throw new IllegalArgumentException("无效的空间类型: " + value + "，有效值: [0=私有空间, 1=团队空间]");
    }
}