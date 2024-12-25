package com.prodigal.system.api.imagesearch.baidu;

import com.prodigal.system.exception.BusinessException;
import com.prodigal.system.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 获取相似图片的地址 step 2
 **/
@Slf4j
public class GetImageFirstUrlApi {
    public static String getImageFirstUrl(String url) {
        //使用jsoup 对地址进行 爬取
        try{
            Document document = Jsoup.connect(url).timeout(5000).get();
            //获取所有的<script> 标签
            Elements scriptElements = document.getElementsByTag("script");
            //遍历 ，找到含 firstUrl 的脚本内容
            for (Element script : scriptElements) {
                String html = script.html();
                if (html.contains("\"firstUrl\"")){
                    //正则表达式提取 firsturl 的值
                    Pattern pattern = Pattern.compile("\"firstUrl\":\"(.*?)\"");
                    Matcher matcher = pattern.matcher(html);
                    if (matcher.find()){
                        String firstUrl = matcher.group(1);
                        //将转义字符 进行解码  "firstUrl": "https:\/\/graph.baidu.com\/ajax\/pcsimi?carousel=503&entrance=GENERAL&extUiData%5BisLogoShow%5D=1&inspire=general_pc&limit=30&next=2&render_type=card&session_id=10296902868594392131&sign=12696e97cd54acd88139901735006368&tk=bebc1&tpl_from=pc",
                        return firstUrl.replace("\\/", "/");
                    }
                }
            }
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"以图搜图失败-未找到 url");
        }catch (Exception e){
            log.error(String.format("以图搜图  请求地址:%s  失败：%s",url,e));
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"以图搜图失败:"+e.getMessage());
        }
    }

    public static void main(String[] args) {
        String url="https://graph.baidu.com/s?card_key=&entrance=GENERAL&extUiData[isLogoShow]=1&f=all&isLogoShow=1&session_id=7217195466184790954&sign=12695e97cd54acd88139901735007493&tpl_from=pc";
        String imageFirstUrl = getImageFirstUrl(url);
        log.info("imageFirstUrl:{}",imageFirstUrl);
    }
}
