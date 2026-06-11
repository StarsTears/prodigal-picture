package com.prodigal.system.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import org.springframework.util.ObjectUtils;

import java.util.Objects;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 图片审核状态枚举
 **/
@Getter
public enum PictureReviewStatusEnum {
    REVIEWING("待审核",0),
    PASS("审核通过",1),
    REJECT("已拒绝",2);
    private final String text;
    private final int value;

    PictureReviewStatusEnum(String text, int value) {
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
    public static PictureReviewStatusEnum getEnumByValue(Integer value) {
        if (ObjectUtils.isEmpty(value)){
            return null;
        }
        for (PictureReviewStatusEnum userRoleEnum : PictureReviewStatusEnum.values()){
            if (Objects.equals(userRoleEnum.value, value)){
                return userRoleEnum;
            }
        }
        throw new IllegalArgumentException("无效的审核状态: " + value + "，有效值: [0=待审核, 1=审核通过, 2=已拒绝]");
    }

}
