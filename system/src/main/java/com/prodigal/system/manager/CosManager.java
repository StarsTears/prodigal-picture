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

import jakarta.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
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
     * 下载对象（附带元数据，用于获取 Content-Length 和 Content-Type）
     * @param key 唯一键
     */
    public ObjectMetadata getObjectMetadata(String key) {
        String cosKey = key.startsWith("/") ? key.substring(1) : key;
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), cosKey);
        return cosClient.getObject(getObjectRequest).getObjectMetadata();
    }

    /**
     * 流式下载对象到指定输出流（不加载整个文件到内存）
     * @param key COS key（可带前导 /）
     * @param out 目标输出流
     */
    public void streamToOutput(String key, OutputStream out) throws IOException {
        String cosKey = key.startsWith("/") ? key.substring(1) : key;
        GetObjectRequest request = new GetObjectRequest(cosClientConfig.getBucket(), cosKey);
        COSObject cosObject = cosClient.getObject(request);
        try (COSObjectInputStream in = cosObject.getObjectContent()) {
            byte[] buffer = new byte[8192];
            int n;
            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
            out.flush();
        }
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
            log.error("COS service error", e);
        } catch (CosClientException e) {
            log.error("COS client error", e);
        }
    }

    /**
     * 获取临时下载地址（预签名 URL，不暴露 bucket/region 信息）
     * @param key 图片在数据库中存储的路径，以 / 开头的相对路径
     * @return 临时预签名下载 URL
     */
    public String generateTempUrl(String key){
        // DB 中存储的路径以 / 开头，COS 实际 key 不含前导 /
        String cosKey = key.startsWith("/") ? key.substring(1) : key;
        String bucketName = cosClientConfig.getBucket();
        Date expirationDate = new Date(System.currentTimeMillis() + 30 * 60 * 1000);
        Map<String, String> params = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        HttpMethodName method = HttpMethodName.GET;
        URL url = cosClient.generatePresignedUrl(bucketName, cosKey, expirationDate, method, headers, params);
        log.info("图片:{} 临时下载地址：{}", key, url.toString());
        return url.toString();
    }

}
