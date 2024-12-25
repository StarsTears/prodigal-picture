package com.prodigal.system.api.imagesearch.baidu;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.prodigal.system.api.imagesearch.ImageSearchResult;
import com.prodigal.system.exception.BusinessException;
import com.prodigal.system.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 获取相似图片的地址 step3
 **/
@Slf4j
public class GetImageUrlApi {
    public static List<ImageSearchResult> getImageUrl(String url) {
        try {
            HttpResponse response = HttpUtil.createGet(url).execute();
            if (!response.isOk()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"以图搜图失败:" + response.getStatus());
            }
            String body = response.body();
            List<ImageSearchResult> imageResult= processResponse(body);
            return imageResult;
        }catch (Exception e){
            log.error("以图搜图-图片列表获取失败:{}",e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"以图搜图-图片列表获取失败:" + e.getMessage());
        }
    }

    private static List<ImageSearchResult> processResponse(String body) {
        List<ImageSearchResult> response = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(body);
        if (!jsonObject.containsKey("data")){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"以图搜图-图片列表获取失败:data不存在");
        }
        JSONObject data = jsonObject.getJSONObject("data");
        if (!data.containsKey("list")) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "以图搜图-图片列表获取失败:list不存在");
        }
        JSONArray list = data.getJSONArray("list");
        return JSONUtil.toList(list, ImageSearchResult.class);
    }

    public static void main(String[] args) {
        String url = "https://graph.baidu.com/ajax/pcsimi?carousel=503&entrance=GENERAL&extUiData%5BisLogoShow%5D=1&inspire=general_pc&limit=30&next=2&render_type=card&session_id=7217195466184790954&sign=12695e97cd54acd88139901735007493&tk=1f25b&tpl_from=pc";
        List<ImageSearchResult> imageUrl = getImageUrl(url);
        log.info("imageUrl:{}",imageUrl);
    }
}
