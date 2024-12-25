package com.prodigal.system.api.imagesearch;

import com.prodigal.system.api.imagesearch.baidu.GetImageFirstUrlApi;
import com.prodigal.system.api.imagesearch.baidu.GetImagePageUrlApi;
import com.prodigal.system.api.imagesearch.baidu.GetImageUrlApi;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @create: 2024-12-24 11:04
 * @description: 百度图片搜索服务-仅支持 jpeg/png
 **/
@Slf4j
public class BaiduImageSearchApiFaced {
    public static List<ImageSearchResult> searchImage(String imageUrl) {
        //step1 获取图片页面地址
        String imagePageUrl = GetImagePageUrlApi.getImagePageUrl(imageUrl);
        //step2 获取图片地址
        String imageFirstUrl = GetImageFirstUrlApi.getImageFirstUrl(imagePageUrl);
        //step3 获取图片
        return GetImageUrlApi.getImageUrl(imageFirstUrl);
    }

    public static void main(String[] args) {
        String url="https://www.codefather.cn/logo.png";
        List<ImageSearchResult> imageSearchResults = searchImage(url);
        log.info("imageSearchResults:{}",imageSearchResults);
    }
}
