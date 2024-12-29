package com.prodigal.system.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 邮箱校验规则
 **/
public class EmailValidatorUtils {
    // 定义邮箱的正则表达式
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

    // 编译正则表达式
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    /**
     * 校验邮箱地址是否正确
     *
     * @param email 待校验的邮箱地址
     * @return 如果邮箱地址正确，则返回 true；否则返回 false
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        return matcher.matches();
    }
    public static void main(String[] args) {
        // 测试邮箱地址
        String[] testEmails = {
                "test@example.com",
                "user.name+tag+sorting@example.com",
                "user@sub.example.com",
                "user@123.123.123.123",
                "user@[IPv6:2001:db8::1]",
                "plainaddress",
                "@missingusername.com",
                "username@.com.my",
                "username@.com",
                "username@com",
                "username@domain..com",
                ""
        };

        for (String email : testEmails) {
            System.out.println("Is '" + email + "' a valid email? " + isValidEmail(email));
        }
    }
}
