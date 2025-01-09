package com.prodigal.system.manager.websocket.model;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

import java.util.Objects;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @create: 2025-01-07 16:59
 * @description: 图片编辑操作类型
 **/
@Getter
public enum PictureEditActionEnum {
    ZOOM_IN("放大", "ZOOM_IN"),
    ZOOM_OUT("缩小", "ZOOM_OUT"),
    ROTATE_LEFT("左旋", "ROTATE_LEFT"),
    ROTATE_RIGHT("右旋", "ROTATE_RIGHT"),
    ;
    private final String text;
    private final String value;

    PictureEditActionEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据value 获取枚举值
     */
    public static PictureEditActionEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)){
            return null;
        }
        for (PictureEditActionEnum actionEnum : PictureEditActionEnum.values()){
            if (Objects.equals(actionEnum.value, value)){
                return actionEnum;
            }
        }
        return null;
    }
}
