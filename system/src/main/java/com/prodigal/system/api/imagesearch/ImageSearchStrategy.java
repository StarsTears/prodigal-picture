package com.prodigal.system.api.imagesearch;

import java.util.List;

/**
 * 以图搜图策略接口
 */
public interface ImageSearchStrategy {

    /**
     * 根据图片 URL 搜索相似图片
     *
     * @param imageUrl 图片地址
     * @return 相似图片列表，失败时返回空列表而非 null
     */
    List<ImageSearchResult> searchImage(String imageUrl);
}
