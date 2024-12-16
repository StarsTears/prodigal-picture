package com.prodigal.system;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import com.prodigal.system.model.enums.UserRoleEnum;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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
        if (matcher.find()) {
            System.out.println("存在特殊字符");
        } else {
            System.out.println("不存在特殊字符");
        }
    }

    @Test
    void testPermissionCov() {
        String role = "admin,user";
        if (role == null || role.trim().isEmpty()) {
            // 如果输入的 role 为 null 或空字符串，可以选择抛出异常，或者返回空集合
            log.error("role:为空");  // 也可以选择抛出 IllegalArgumentException
        }
        List<String> mustRoles;
        if (role.contains(",")) {
            mustRoles = Arrays.stream(role.split(",")).map(String::trim).collect(Collectors.toList());
        } else {
            mustRoles = Collections.singletonList(role.trim());
        }
        Set<UserRoleEnum> collect = mustRoles.stream().map(UserRoleEnum::getEnumByValue).filter(Objects::nonNull).collect(Collectors.toSet());
        log.info("mustRoles:" + mustRoles);
        for (UserRoleEnum userRoleEnum : collect) {
            log.info("userRoleEnum:" + userRoleEnum.getRole());
        }
    }

    @Test
    void testUrl() throws UnsupportedEncodingException {
        String url = "https://cn.bing.com/images/search?view=detailV2&ccid=iXpCesSY&id=29C935A884F5F9BDB0C70474BBF320847FD289DE&thid=OIP.iXpCesSY3U3dN8ZQCBNYogHaM9&mediaurl=https%3a%2f%2fts1.cn.mm.bing.net%2fth%2fid%2fR-C.897a427ac498dd4ddd37c650081358a2%3frik%3d3onSf4Qg87t0BA%26riu%3dhttp%253a%252f%252fimg.sucaijishi.com%252fuploadfile%252f2022%252f1102%252f20221102121120598.jpg%253fimageMogr2%252fformat%252fjpg%252fblur%252f1x0%252fquality%252f60%26ehk%3dvXEue%252b2fB0dfqiJ1sv5%252beGjmoDaIySEMY9KLnp2%252beKw%253d%26risl%3d%26pid%3dImgRaw%26r%3d0&exph=3584&expw=2048&q=%e2%80%98%e4%ba%8c%e6%ac%a1%e5%85%83%e2%80%99&simid=607987552684344242&FORM=IRPRST&ck=5C17D80AD1CBDCA8E5CC2775E47612D3&selectedIndex=16&itb=0";
        // 截取URL参数部分
        String dUrl = URLDecoder.decode(url, "UTF-8");
        String query = dUrl.substring(dUrl.indexOf('?') + 1);

        // 将URL参数解析为JSON对象
        Map<String,String> json = new HashMap<>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            try {
                json.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Test
   void testFileUtil(){
       String url =  "http://pic.qianye88.com/4kdongman358a5af0-87a6-351f-9a04-16107055ab3b.jpg";
        System.out.println(String.format("%s.%s",FileUtil.mainName(url), FileUtil.getSuffix(url)));;
    }
}
