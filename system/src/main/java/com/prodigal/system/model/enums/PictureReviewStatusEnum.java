package com.prodigal.system.model.enums;

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

    /**
     * 根据value 获取枚举值
     */
    public static PictureReviewStatusEnum getEnumByValue(Integer value) {
        if (ObjectUtils.isEmpty(value)){
            return null;
        }
        for (PictureReviewStatusEnum userRoleEnum : PictureReviewStatusEnum.values()){
            if (Objects.equals(userRoleEnum.value, value)){
                return userRoleEnum;
            }
        }
        return null;
    }

}
