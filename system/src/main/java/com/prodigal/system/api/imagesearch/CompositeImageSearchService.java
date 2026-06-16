package com.prodigal.system.api.imagesearch;

import com.prodigal.system.api.imagesearch.strategy.BingImageSearchStrategy;
import com.prodigal.system.api.imagesearch.strategy.GoogleImageSearchStrategy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 多平台以图搜图编排器
 * 按 Bing → Google 优先级降级，任一成功即返回
 */
@Slf4j
@Service
public class CompositeImageSearchService {

    @Resource
    private BingImageSearchStrategy bingImageSearchStrategy;

    @Resource
    private GoogleImageSearchStrategy googleImageSearchStrategy;

    public List<ImageSearchResult> searchImage(String imageUrl) {

        // 优先 Google 官方 API

        List<ImageSearchResult> results = googleImageSearchStrategy.searchImage(imageUrl);
        if (!results.isEmpty()) {
            log.info("以图搜图 → Google 命中 {} 条", results.size());
            return results;
        }
        log.info("以图搜图 → Google 无结果，降级 Bing");
        // 降级 Bing
        results = bingImageSearchStrategy.searchImage(imageUrl);
        if (!results.isEmpty()) {
            log.info("以图搜图 → Bing 命中 {} 条", results.size());
            return results;
        }

        log.warn("以图搜图 → 所有平台均无结果");
        return Collections.emptyList();
    }
}
