package com.prodigal.system.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 替代因 swagger-ui.enabled=false 被禁用的 SwaggerConfigResource，
 * 自动读取 springdoc.group-configs 配置，支持多分组。
 */
@RestController
@ConfigurationProperties(prefix = "springdoc")
public class SwaggerConfigController {

    private final String apiDocsPath;
    private List<Map<String, Object>> groupConfigs = new ArrayList<>();

    public SwaggerConfigController(
            @Value("${springdoc.api-docs.path:/v3/api-docs}") String apiDocsPath) {
        this.apiDocsPath = apiDocsPath;
    }

    public void setGroupConfigs(List<Map<String, Object>> groupConfigs) {
        this.groupConfigs = groupConfigs;
    }

    @ResponseBody
    @GetMapping("${springdoc.api-docs.path:/v3/api-docs}/swagger-config")
    public Map<String, Object> swaggerConfig(HttpServletRequest request) {
        String prefix = request.getContextPath();
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("configUrl", prefix + apiDocsPath + "/swagger-config");

        List<Map<String, String>> urls = new ArrayList<>();
        for (Map<String, Object> gc : groupConfigs) {
            String group = (String) gc.get("group");
            Map<String, String> entry = new LinkedHashMap<>();
            entry.put("url", prefix + apiDocsPath + "/" + group);
            entry.put("name", group);
            urls.add(entry);
        }
        config.put("urls", urls);

        return config;
    }
}
