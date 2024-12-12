package com.prodigal.system;

import com.prodigal.system.model.enums.UserRoleEnum;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
@Slf4j
@SpringBootTest
class SystemApplicationTests {

    /**
     * 特殊字符校验
     */
    @Test
    void contextLoads() {
        String regex = "[@#$%^&*(),.?\":{}|<>]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher("嗨咯(");
        if (matcher.find()){
            System.out.println("存在特殊字符");
        }else {
            System.out.println("不存在特殊字符");
        }
    }
    @Test
    void testPermissionCov(){
        String role = "admin,user";
        if (role == null || role.trim().isEmpty()) {
            // 如果输入的 role 为 null 或空字符串，可以选择抛出异常，或者返回空集合
            log.error("role:为空");  // 也可以选择抛出 IllegalArgumentException
        }
        List<String> mustRoles;
        if (role.contains(",")){
            mustRoles = Arrays.stream(role.split(",")).map(String::trim).collect(Collectors.toList());
        }else{
            mustRoles = Collections.singletonList(role.trim());
        }
        Set<UserRoleEnum> collect = mustRoles.stream().map(UserRoleEnum::getEnumByValue).filter(Objects::nonNull).collect(Collectors.toSet());
        log.info("mustRoles:"+mustRoles);
        for (UserRoleEnum userRoleEnum : collect) {
            log.info("userRoleEnum:"+userRoleEnum.getRole());
        }
    }

}
