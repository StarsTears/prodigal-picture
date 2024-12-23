package com.prodigal.system;

import cn.hutool.core.util.StrUtil;
import com.prodigal.system.config.CosClientConfig;
import com.prodigal.system.exception.BusinessException;
import com.prodigal.system.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @create: 2024-12-17 10:00
 * @description: 图片测试
 **/
@Slf4j
@SpringBootTest
public class PictureTest {
    @Test
    void arrayToMap() {
        // 截取URL参数部分
        String href = "https://cn.bing.com/images/search?view=detailV2&ccid=qvtE6A0Z&id=DEB7A51420387E9FCFBF4EBB94F81429AFB496AE&thid=OIP.qvtE6A0Z9M42mea0tCx50wHaNK&mediaurl=https%3a%2f%2fc-ssl.duitang.com%2fuploads%2fitem%2f202003%2f09%2f20200309065807_olglw.jpeg&exph=1920&expw=1080&q=%27%e4%ba%8c%e6%ac%a1%e5%85%83%e5%a3%81%e7%ba%b8%27&simid=608021989704298475&FORM=IRPRST&ck=730C4A900C748D97D4D45F242DA168A1&selectedIndex=30&itb=0";
        String dUrl = null;
        try {
            dUrl = URLDecoder.decode(href, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("URL解码报错",e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"URL解码报错哦");
        }
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
        //处理图片url.防止出现转义错误
        String fileUrl= json.get("riu");
        if (StrUtil.isBlank(fileUrl)){
            fileUrl = json.get("mediaurl");
        }
        int questionMarkIndex = fileUrl.indexOf("?");
        if (questionMarkIndex > -1){
            fileUrl = fileUrl.substring(0,questionMarkIndex);
        }
        log.info("fileUrl:{}",fileUrl);
    }

    @Test
    void test(){
       String url="https%3a%2f%2fclubimg.club.vmall.com%2fdata%2fattachment%2fforum%2f202001%2f19%2f010303zuxgzrj3fijcymu2.jpg";
        try {
            log.info("sourceUrl:{}",URLDecoder.decode(url,"UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    @Resource
    private CosClientConfig cosClientConfig;
    @Test
    void processUrl(){
        List<String> keys = new ArrayList<>();
        keys.add("https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com//picture/public/1867061940399714305/2024-12-16_MECJENCC.");
        keys.add("https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com//picture/public/1867061940399714305/2024-12-16_MECJENCC.");
        keys.add("https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com//picture/public/1867061940399714305/2024-12-16_MECJENCC.");
        String hostBucket = cosClientConfig.getHost() + "//" + cosClientConfig.getBucket();
        System.out.println(hostBucket.length());
        List<String> collect = keys.stream().map(key -> key.substring(hostBucket.length() + 1)).collect(Collectors.toList());
        log.info("collect:{}",collect);
    }
}
