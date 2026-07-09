package com.prodigal.system.manager.upload;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.prodigal.system.exception.BizStatus;
import com.prodigal.system.exception.BusinessException;
import com.prodigal.system.manager.CosManager;
import com.prodigal.system.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 图片上传模板抽象类
 **/
@Slf4j
public abstract class PictureUploadTemplate {

    @Resource
    private CosManager cosManager;

    /**
     * 图片上传
     * @param inputSource 数据源
     * @param uploadFilePrefix 文件上传前缀
     */
    public UploadPictureResult uploadPicture(Object inputSource,String uploadFilePrefix ) {
        //校验图片
        verifyPicture(inputSource);
        //解析并返回结果
        //图片上传地址
        String uuid = RandomUtil.randomString(8);
        String originalFilename = this.getOriginalFilename(inputSource);
        File file = null;

        try {
            //创建临时文件，先下载再根据实际内容确定扩展名
            file = File.createTempFile("upload", null);
            this.processFile(inputSource,file);

            //根据文件魔数检测真实格式，避免 URL 无扩展名或扩展名错配
            String detectedType = FileUtil.getType(file);
            String extension = (detectedType != null && detectedType.matches("jpg|jpeg|png|webp")) ? detectedType : "jpeg";

            String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, extension);
            String uploadPath = String.format("%s/%s", uploadFilePrefix, uploadFileName);

            //上传图片 后 获得图片信息对象
            PutObjectResult putPictureObjectResult = cosManager.putPictureObject(uploadPath, file);
            ImageInfo imageInfo = putPictureObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            //图片处理后的返回结果
            ProcessResults processResults = putPictureObjectResult.getCiUploadResult().getProcessResults();
            List<CIObject> objectList = processResults.getObjectList();
            if (CollUtil.isNotEmpty(objectList)){
                CIObject compressedObject = objectList.get(0);
                //缩略图默认为压缩图
                CIObject thumbnailObject = compressedObject;
                if (objectList.size()>1) {
                    thumbnailObject = objectList.get(1);
                }
                UploadPictureResult uploadPictureResult = buildResult(originalFilename, uploadPath, compressedObject, thumbnailObject,imageInfo);
                this.processUploadResult(inputSource,uploadPictureResult);
                return uploadPictureResult;
            }
            //封装返回结果
            UploadPictureResult uploadPictureResult = buildResult(imageInfo, file, originalFilename, uploadPath);
            //源文件地址
            this.processUploadResult(inputSource,uploadPictureResult);
            return uploadPictureResult;
        } catch (IOException e) {
            log.error("file upload COS error", e);
            throw new BusinessException(BizStatus.SYSTEM_ERROR, "上传失败");
        } finally {
            //删除临时文件
            this.deleteTempFile(file);
        }
    }

    protected abstract void processUploadResult(Object inputSource, UploadPictureResult uploadPictureResult);

    /**
     * 校验图片
     */
    protected abstract void verifyPicture(Object inputSource);

    /**
     * 获取文件名
     */
    protected abstract String getOriginalFilename(Object inputSource);

    /**
     * 处理文件（将输入源存入本地临时文件）
     * @param inputSource 数据源
     * @param file  临时文件
     */
    protected abstract void processFile(Object inputSource,File file) throws IOException;

    /**
     * 封装返回结果
     * 调整url: 不拼接 前缀 host: XXX.com;前台直接通过代理转发获取
     * @param originalFilename 文件名
     * @param compressedObject 图片处理返回结果
     * @param thumbnailObject 图片缩略后的返回结果
     * @return UploadPictureResult
     */
    private UploadPictureResult buildResult(String originalFilename, String uploadPath, CIObject compressedObject, CIObject thumbnailObject,ImageInfo imageInfo) {

        int picWidth = compressedObject.getWidth();
        int picHeight = compressedObject.getHeight();
        //宽高比
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        //封装返回结果
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        //未压缩的原图地址
        uploadPictureResult.setOriginUrl( "/" +uploadPath);
        //格式转换后的图片地址
        uploadPictureResult.setUrl("/" + compressedObject.getKey());
        //缩略图地址
        uploadPictureResult.setThumbnailUrl( "/" + thumbnailObject.getKey());
        //通过url 上传的url路径
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(compressedObject.getFormat());
        uploadPictureResult.setPicSize(compressedObject.getSize().longValue());
        uploadPictureResult.setPicColor(imageInfo.getAve());//颜色的格式为 16 进制
        return uploadPictureResult;
    }
    /**
     * 封装返回结果
     * 调整url: 不拼接 前缀 host: XXX.com;前台直接通过代理转发获取
     * @param imageInfo 图片信息对象
     * @param file  文件
     * @param originalFilename  文件名
     * @param uploadPath  上传地址
     */
    private UploadPictureResult buildResult(ImageInfo imageInfo, File file,String originalFilename, String uploadPath) {
        int picWidth = imageInfo.getWidth();
        int picHeight = imageInfo.getHeight();
        //宽高比
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        //封装返回结果
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        uploadPictureResult.setUrl( "/" + uploadPath);
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(imageInfo.getFormat());
        uploadPictureResult.setPicSize(FileUtil.size(file));
        uploadPictureResult.setSourceUrl(originalFilename);
        return uploadPictureResult;
    }

    /**
     * 删除临时文件
     */
    public void deleteTempFile(File file) {
        if (file == null) return;
        boolean delete = file.delete();
        if (!delete)
            log.error("file delete error,filepath:{}", file.getAbsolutePath());
    }
}
