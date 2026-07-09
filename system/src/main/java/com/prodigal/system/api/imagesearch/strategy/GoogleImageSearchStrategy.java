package com.prodigal.system.api.imagesearch.strategy;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
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

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

/**
 * Google Cloud Vision API 以图搜图（WEB_DETECTION）
 */
@Slf4j
@Component
public class GoogleImageSearchStrategy implements ImageSearchStrategy {

    private static final String API_URL = "https://vision.googleapis.com/v1/images:annotate";

    @Value("${google.api.key:}")
    private String apiKey;

    @Value("${google.api.proxy-host:}")
    private String proxyHost;

    @Value("${google.api.proxy-port:0}")
    private int proxyPort;

    @Value("${google.api.proxy-type:HTTP}")
    private String proxyType;

    @Resource
    private CosManager cosManager;

    @Override
    public List<ImageSearchResult> searchImage(String imageUrl) {
        if (StrUtil.isBlank(apiKey)) {
            log.debug("Google API Key 未配置，跳过");
            return Collections.emptyList();
        }
        try {
            byte[] imageBytes = downloadImageBytes(imageUrl);
            String base64 = Base64.getEncoder().encodeToString(imageBytes);

            HttpRequest httpRequest = HttpRequest.post(API_URL + "?key=" + apiKey)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .body(buildRequestBody(base64))
                    .timeout(5000);

            if (StrUtil.isNotBlank(proxyHost) && proxyPort > 0) {
                Proxy.Type type = "SOCKS".equalsIgnoreCase(proxyType) ? Proxy.Type.SOCKS : Proxy.Type.HTTP;
                httpRequest.setProxy(new Proxy(type, new InetSocketAddress(proxyHost, proxyPort)));
            }

            HttpResponse response = httpRequest.execute();

            if (!response.isOk()) {
                log.warn("Google 以图搜图返回非 200: status={}, body={}", response.getStatus(), response.body());
                return Collections.emptyList();
            }
            return parseResponse(response.body());
        } catch (Exception e) {
            log.error("Google 以图搜图异常: imageUrl={}, error={}", imageUrl, e.getMessage());
            return Collections.emptyList();
        }
    }

    private byte[] downloadImageBytes(String imageUrl) {
        if (StrUtil.isNotBlank(imageUrl) && imageUrl.startsWith("/picture/")) {
            try {
                return cosManager.getObjectBytes(imageUrl);
            } catch (Exception e) {
                log.warn("Google 从 COS 下载图片失败，尝试作为 URL 下载: error={}", e.getMessage());
            }
        }
        HttpResponse response = HttpUtil.createGet(imageUrl)
                .timeout(10000)
                .execute();
        return response.bodyBytes();
    }

    private String buildRequestBody(String base64Image) {
        JSONObject image = new JSONObject();
        image.set("content", base64Image);

        JSONObject feature = new JSONObject();
        feature.set("type", "WEB_DETECTION");
        feature.set("maxResults", 20);

        JSONObject request = new JSONObject();
        request.set("image", image);
        request.set("features", new JSONArray().put(feature));

        JSONObject body = new JSONObject();
        body.set("requests", new JSONArray().put(request));
        return body.toString();
    }

    private List<ImageSearchResult> parseResponse(String body) {
        List<ImageSearchResult> results = new ArrayList<>();
        JSONObject json = JSONUtil.parseObj(body);
        JSONArray responses = json.getJSONArray("responses");
        if (responses == null || responses.isEmpty()) {
            return results;
        }
        JSONObject firstResponse = responses.getJSONObject(0);
        if (firstResponse.containsKey("error")) {
            log.warn("Google 以图搜图返回 error: {}", firstResponse.getJSONObject("error"));
            return results;
        }
        JSONObject webDetection = firstResponse.getJSONObject("webDetection");
        if (webDetection == null) {
            return results;
        }

        JSONArray visuallySimilar = webDetection.getJSONArray("visuallySimilarImages");
        if (visuallySimilar != null) {
            for (int i = 0; i < visuallySimilar.size(); i++) {
                JSONObject item = visuallySimilar.getJSONObject(i);
                String url = item.getStr("url");
                if (StrUtil.isBlank(url)) continue;
                ImageSearchResult result = new ImageSearchResult();
                result.setThumbUrl(url);
                result.setObjUrl(url);
                result.setFromUrl(url);
                results.add(result);
            }
        }
        return results;
    }
}
