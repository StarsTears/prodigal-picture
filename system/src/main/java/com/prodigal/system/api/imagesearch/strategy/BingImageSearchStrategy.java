package com.prodigal.system.api.imagesearch.strategy;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.prodigal.system.api.imagesearch.ImageSearchResult;
import com.prodigal.system.api.imagesearch.ImageSearchStrategy;
import com.prodigal.system.manager.CosManager;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Bing Visual Search 以图搜图（官方 API）
 */
@Slf4j
@Component
public class BingImageSearchStrategy implements ImageSearchStrategy {

    private static final String API_URL = "https://api.bing.microsoft.com/v7.0/images/visualsearch";

    @Value("${bing.api.key:}")
    private String subscriptionKey;

    @Resource
    private CosManager cosManager;

    @Override
    public List<ImageSearchResult> searchImage(String imageUrl) {
        if (StrUtil.isBlank(subscriptionKey)) {
            log.debug("Bing API Key 未配置，跳过");
            return Collections.emptyList();
        }
        try {
            HttpResponse response;
            if (StrUtil.isNotBlank(imageUrl) && imageUrl.startsWith("/picture/")) {
                response = executeWithBinary(imageUrl);
            } else {
                response = executeWithUrl(imageUrl);
            }
            assert response != null;
            if (!response.isOk()) {
                log.warn("Bing 以图搜图返回非 200: status={}, body={}", response.getStatus(), response.body());
                return Collections.emptyList();
            }
            return parseResponse(response.body());
        } catch (Exception e) {
            log.error("Bing 以图搜图异常: imageUrl={}, error={}", imageUrl, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 二进制方式上传（从 COS 下载字节后以 multipart 上传至 Bing）
     */
    private HttpResponse executeWithBinary(String cosKey) {
        try {
            byte[] imageBytes = cosManager.getObjectBytes(cosKey);
            return HttpRequest.post(API_URL)
                    .header("Ocp-Apim-Subscription-Key", subscriptionKey)
                    .form("image", "image", imageBytes)
                    .timeout(10000)
                    .execute();
        } catch (Exception e) {
            log.warn("Bing 二进制上传失败，降级为 URL 方式: cosKey={}, error={}", cosKey, e.getMessage());
            return executeWithUrl(cosKey);
        }
    }

    /**
     * URL 方式上传（直接传递图片 URL 给 Bing）
     */
    private HttpResponse executeWithUrl(String imageUrl) {
        return HttpRequest.post(API_URL)
                .header("Ocp-Apim-Subscription-Key", subscriptionKey)
                .form("image", imageUrl)
                .timeout(10000)
                .execute();
    }

    private List<ImageSearchResult> parseResponse(String body) {
        List<ImageSearchResult> results = new ArrayList<>();
        JSONObject json = JSONUtil.parseObj(body);
        JSONArray tags = json.getJSONArray("tags");
        if (tags == null || tags.isEmpty()) {
            return results;
        }
        for (int i = 0; i < tags.size(); i++) {
            JSONObject tag = tags.getJSONObject(i);
            JSONArray actions = tag.getJSONArray("actions");
            if (actions == null) continue;
            for (int j = 0; j < actions.size(); j++) {
                JSONObject action = actions.getJSONObject(j);
                if (!"VisualSearch".equals(action.getStr("actionType"))) continue;
                JSONObject data = action.getJSONObject("data");
                if (data == null) continue;
                JSONArray value = data.getJSONArray("value");
                if (value == null) continue;
                for (int k = 0; k < value.size(); k++) {
                    JSONObject item = value.getJSONObject(k);
                    ImageSearchResult result = new ImageSearchResult();
                    result.setThumbUrl(item.getStr("thumbnailUrl"));
                    result.setObjUrl(item.getStr("contentUrl"));
                    result.setFromUrl(item.getStr("hostPageUrl"));
                    results.add(result);
                }
            }
        }
        return results;
    }
}
