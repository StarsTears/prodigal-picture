package com.prodigal.system.manager.websocket.model;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

import java.util.Objects;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @create: 2025-01-07 16:54
 * @description: 图片编辑消息类型
 **/
@Getter
public enum PictureEditMessageTypeEnum {
    INFO("发送通知", "INFO"),
    ERROR("发送错误", "ERROR"),
    ENTER_EDIT("进入编辑状态", "ENTER_EDIT"),
    EXIT_EDIT("退出编辑状态", "EXIT_EDIT"),
    EDIT_ACTION("执行编辑操作", "EDIT_ACTION")
    ;
    private final String text;
    private final String value;

    PictureEditMessageTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据value 获取枚举值
     */
    public static PictureEditMessageTypeEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)){
            return null;
        }
        for (PictureEditMessageTypeEnum messageTypeEnum : PictureEditMessageTypeEnum.values()){
            if (Objects.equals(messageTypeEnum.value, value)){
                return messageTypeEnum;
            }
        }
        return null;
    }
}
