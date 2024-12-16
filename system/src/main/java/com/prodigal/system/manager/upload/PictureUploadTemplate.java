package com.prodigal.system.manager.upload;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.prodigal.system.config.CosClientConfig;
import com.prodigal.system.exception.BusinessException;
import com.prodigal.system.exception.ErrorCode;
import com.prodigal.system.exception.ThrowUtils;
import com.prodigal.system.manager.CosManager;
import com.prodigal.system.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
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
    private CosClientConfig cosClientConfig;

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
        //拼接上传路径，避免文件名重复;增加安全性
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("/%s/%S", uploadFilePrefix, uploadFileName);
        File file = null;

        try {
            //创建临时文件
            file = File.createTempFile(uploadPath, null);
            this.processFile(inputSource,file);
            //上传图片 后 获得图片信息对象
            PutObjectResult putPictureObjectResult = cosManager.putPictureObject(uploadPath, file);
            ImageInfo imageInfo = putPictureObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            //封装返回结果
            return buildResult( imageInfo, file,originalFilename, uploadPath);
        } catch (IOException e) {
            log.error("file upload COS error", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            //删除临时文件
            this.deleteTempFile(file);
        }
    }

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
     * @param imageInfo 图片信息对象
     * @param file  文件
     * @param originalFilename  文件名
     * @param uploadPath  上传地址
     */
    private UploadPictureResult buildResult(ImageInfo imageInfo, File file,String originalFilename, String uploadPath) {
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        int picWidth = imageInfo.getWidth();
        int picHeight = imageInfo.getHeight();
        //宽高比
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(imageInfo.getFormat());
        uploadPictureResult.setPicSize(FileUtil.size(file));
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
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
