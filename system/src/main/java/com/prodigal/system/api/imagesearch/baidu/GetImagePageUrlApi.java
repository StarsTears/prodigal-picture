package com.prodigal.system.api.imagesearch.baidu;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.prodigal.system.exception.BusinessException;
import com.prodigal.system.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 获取给定 图片url 相似图片的url地址 step 1
 **/
@Slf4j
public class GetImagePageUrlApi {
    public static final String API_URL = "https://graph.baidu.com/upload?uptime=";
    public static String getImagePageUrl(String imageUrl){
        //构造请求参数
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("image", imageUrl);
        requestParams.put("tn", "pc");
        requestParams.put("from", "pc");
        requestParams.put("image_source", "PC_UPLOAD_URL");
        long uptime = System.currentTimeMillis();
        String url = API_URL + uptime;
        try {
            HttpResponse response = HttpRequest.post(url)
                    .form(requestParams)
                    .timeout(5000)
                    .execute();
            if (!response.isOk()){
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"以图搜图失败:"+response.getStatus());
            }
            //解析响应
            String responseBody = response.body();
            Map<String,Object> result = JSONUtil.toBean(responseBody, Map.class);
            if (result == null || !Integer.valueOf(0).equals(result.get("status"))){
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"以图搜图-接口调用失败");
            }
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            String rowUrl = (String) data.get("url");
            //对地址进行解码
            String decodeUrl = URLDecoder.decode(rowUrl, StandardCharsets.UTF_8);
            if (decodeUrl==null){
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"以图搜图-地址解码失败-未返回有效结果");
            }
            return decodeUrl;
        }catch (Exception e){
            log.error(String.format("以图搜图  请求地址:%s  失败：%s",imageUrl,e));
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"以图搜图失败:"+e.getMessage());
        }
    }

    public static void main(String[] args) {
        String url="https://www.codefather.cn/logo.png";
        String imagePageUrl = getImagePageUrl(url);
        log.info("imagePageUrl:{}",imagePageUrl);
    }
}
