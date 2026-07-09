package com.prodigal.system.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import org.springframework.util.ObjectUtils;

import java.util.Objects;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 用户角色枚举类
 **/
@Getter
public enum UserRoleEnum {
    ADMINISTRATOR("administrator", "超级管理员"),
    ADMIN("admin", "管理员"),
    USER("user", "普通用户");
    private final String role;
    private final String value;

    UserRoleEnum(String role, String value) {
        this.role = role;
        this.value = value;
    }

    @JsonValue
    public String getRole() {
        return role;
    }

    /**
     * 根据value 获取枚举值
     * @param value 枚举值的value
     * @return 枚举值
     */
    @JsonCreator
    public static UserRoleEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)){
            return null;
        }
        for (UserRoleEnum userRoleEnum : UserRoleEnum.values()){
            if (Objects.equals(userRoleEnum.role, value)){
                return userRoleEnum;
            }
        }
        throw new IllegalArgumentException("无效的用户角色: " + value + "，有效值: [administrator=超级管理员, admin=管理员, user=普通用户]");
    }
}
