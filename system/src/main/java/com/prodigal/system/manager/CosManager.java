package com.prodigal.system.manager;

import cn.hutool.core.io.FileUtil;
import com.prodigal.system.config.CosClientConfig;
import com.prodigal.system.exception.BusinessException;
import com.prodigal.system.exception.ErrorCode;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.exception.MultiObjectDeleteException;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.*;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: Cos 管理
 **/
@Slf4j
@Component
public class CosManager {
    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    /**
     * 上传对象 COS
     * @param key 唯一键
     * @param file 文件
     */
    public PutObjectResult putObject(String key, File file){
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 下载对象
     * @param key 唯一键
     */
    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }

    /**
     * 上传对象（附带图片信息）
     *
     * @param key  唯一键
     * @param file 文件
     */
    public PutObjectResult putPictureObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        // 对图片进行处理（获取基本信息也被视作为一种处理）
        PicOperations picOperations = new PicOperations();
        // 1 表示返回原图信息
        picOperations.setIsPicInfo(1);
        //指定处理的图片操作规则
        ArrayList<PicOperations.Rule> rules = new ArrayList<>();
        //对原图进行格式转换 webp
        PicOperations.Rule formatRule = new PicOperations.Rule();
        String webpKey = FileUtil.mainName(key) + ".webp";
        formatRule.setFileId(webpKey);
        formatRule.setBucket(cosClientConfig.getBucket());
        formatRule.setRule("imageMogr2/format/webp");
        rules.add(formatRule);
        //对原图进行压缩-缩略 仅对图片大于 20KB 的进行缩略
        if (file.length() > 20L * 1024) {
            PicOperations.Rule thumbnailRule = new PicOperations.Rule();
            String thumbnailKey = FileUtil.mainName(key) + "_thumbnail." + FileUtil.getSuffix(key);
            thumbnailRule.setFileId(thumbnailKey);
            thumbnailRule.setBucket(cosClientConfig.getBucket());
            thumbnailRule.setRule(String.format("imageMogr2/thumbnail/%sx%s>", 256, 256));
            rules.add(thumbnailRule);
        }
        // 构造处理参数
        picOperations.setRules(rules);
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 删除对象-批量删除
     *
     * @param keys  唯一键
     */
    public void deleteObjects(List<String> keys) {
        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(cosClientConfig.getBucket());
        String prefix = cosClientConfig.getHost() + "/";
        List<DeleteObjectsRequest.KeyVersion> keyList = new ArrayList<>();
         keys.stream().forEach(key -> keyList.add(new DeleteObjectsRequest.KeyVersion(key.substring(prefix.length()))));
        deleteObjectsRequest.setKeys(keyList);
        try {
            DeleteObjectsResult deleteObjectsResult = cosClient.deleteObjects(deleteObjectsRequest);
            log.info("Delete objects result: {}", deleteObjectsResult);
        }catch (MultiObjectDeleteException mde) {
            // 如果部分删除成功部分失败, 返回 MultiObjectDeleteException
            List<DeleteObjectsResult.DeletedObject> deleteObjects = mde.getDeletedObjects();
            List<MultiObjectDeleteException.DeleteError> deleteErrors = mde.getErrors();
            log.info("删除COS图片失败:{}", deleteErrors.stream().map(MultiObjectDeleteException.DeleteError::getKey).collect(Collectors.toList()));
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除失败");
        } catch (CosServiceException e) {
            e.printStackTrace();
        } catch (CosClientException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取临时下载地址
     * https://cloud.tencent.com/document/product/436/35217
     * @param key 图片在数据库中存储的路径，以 /picture 开头的路径
     * @return 临时URL
     */
    public String generateTempUrl(String key){

        try {
            // 存储桶的命名格式为 BucketName-APPID，此处填写的存储桶名称必须为此格式
            String bucketName = cosClientConfig.getBucket();
            // 对象键(Key)是对象在存储桶中的唯一标识。详情请参见 [对象键](https://cloud.tencent.com/document/product/436/13324)
            String cosKey = cosClientConfig.getHost() + "/" + key;
            // 设置签名过期时间(可选), 若未进行设置则默认使用 ClientConfig 中的签名过期时间(1小时)
            // 这里设置签名在半个小时后过期
            Date expirationDate = new Date(System.currentTimeMillis() + 30 * 60 * 1000);
            // 填写本次请求的参数，需与实际请求相同，能够防止用户篡改此签名的 HTTP 请求的参数
            Map<String, String> params = new HashMap<String, String>();
            params.put("param1", "value1");

            // 填写本次请求的头部，需与实际请求相同，能够防止用户篡改此签名的 HTTP 请求的头部
            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/json");
            // 请求的 HTTP 方法，上传请求用 PUT，下载请求用 GET，删除请求用 DELETE
            HttpMethodName method = HttpMethodName.GET;
            URL url = cosClient.generatePresignedUrl(bucketName, key, expirationDate, method, headers, params);
            log.info("图片:{} 临时下载地址：{}",key,url.toString());
            return url.toString();
        } catch (CosClientException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            // 7. 关闭客户端，释放资源
            cosClient.shutdown();
        }
    }

}
