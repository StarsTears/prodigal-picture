package com.prodigal.system.manager;

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
import com.prodigal.system.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
 * @description: 通用服务管理
 * @Deprecated:已废弃；改为使用upload包下`PictureUploadTemplate`优化的模板方法
 **/
@Slf4j
@Service
@Deprecated
public class FileManager {
    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    /**
     * 上传图片
     *
     * @param uploadFilePrefix 文件上传前缀
     * @param multipartFile    文件
     */
    public UploadPictureResult uploadPicture(String uploadFilePrefix, MultipartFile multipartFile) {
        //校验图片
        verifyPicture(multipartFile);
        //解析并返回结果
        //图片上传地址
        String uuid = RandomUtil.randomString(8);
        String originalFilename = multipartFile.getOriginalFilename();
        //拼接上传路径，避免文件名重复;增加安全性
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("/%s/%S", uploadFilePrefix, uploadFileName);
        File file = null;

        try {
            //创建临时文件
            file = File.createTempFile(uploadPath, null);
            multipartFile.transferTo(file);
            //上传图片 后 获得图片信息对象
            PutObjectResult putPictureObjectResult = cosManager.putPictureObject(uploadPath, file);
            ImageInfo imageInfo = putPictureObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            //封装返回结果
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
        } catch (IOException e) {
            log.error("file upload COS error", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            //删除临时文件
            this.deleteTempFile(file);
        }
    }

    /**
     * 通过URL上传图片
     *
     * @param fileUrl          图片URL
     * @param uploadFilePrefix 文件上传前缀
     */
    public UploadPictureResult uploadPictureByUrl(String fileUrl, String uploadFilePrefix) {
        //校验图片
        verifyPicture(fileUrl);
        //解析并返回结果
        //图片上传地址
        String uuid = RandomUtil.randomString(8);
//        String originalFilename = multipartFile.getOriginalFilename();
        //拼接上传路径，避免文件名重复;增加安全性
        String originalFilename = FileUtil.mainName(fileUrl);
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("/%s/%S", uploadFilePrefix, uploadFileName);
        File file = null;

        try {
            //创建临时文件
            file = File.createTempFile(uploadPath, null);
//            multipartFile.transferTo(file);
            //下载图片到临时文件
            HttpUtil.downloadFile(fileUrl, file);
            //上传图片 后 获得图片信息对象
            PutObjectResult putPictureObjectResult = cosManager.putPictureObject(uploadPath, file);
            ImageInfo imageInfo = putPictureObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            //封装返回结果
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
     *
     * @param multipartFile 文件
     */
    private void verifyPicture(MultipartFile multipartFile) {
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        //校验文件大小
        long fileSize = multipartFile.getSize();
        final long ONE_M = 1024 * 1024;
        ThrowUtils.throwIf(fileSize > 2 * ONE_M, ErrorCode.PARAMS_ERROR, "文件大小不能超过2M");
        //校验文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        //允许上传文件的后缀
        List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpg", "jpeg", "png", "webp");
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件格式错误");
    }

    /**
     * 校验图片
     *
     * @param fileUrl 文件地址
     */
    private void verifyPicture(String fileUrl) {
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "文件地址不能为空");
        //验证URL格式
        try {
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件地址格式错误");
        }
        //验证URL协议
        ThrowUtils.throwIf(!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://"),
                ErrorCode.PARAMS_ERROR, "文件地址协议错误");

        //通过Head 请求 验证文件是否存在（只能证明一部分；因为有些不具备HEAD请求）
        HttpResponse response = null;

        try {
            response = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
            //未正常返回、无需返回错误
            if (response.getStatus() != HttpStatus.HTTP_OK) {
                return;
            }
            //校验文件类型（图片类型方可：image/jpg,image/jpeg,image/webp,image/png）
            String header = response.header("Content-Type");
            if (header != null){
                final List<String> IMAGE_TYPE =Arrays.asList( "image/jpg","image/jpeg","image/webp","image/png");
                ThrowUtils.throwIf(!IMAGE_TYPE.contains(header),ErrorCode.PARAMS_ERROR,"文件类型错误");
            }
            //校验文件类型大小
            String contentLengthStr = response.header("Content-Length");
            if (StrUtil.isNotBlank(contentLengthStr)){
                try {
                    long contentLength = Long.parseLong(contentLengthStr);
                    final long ONE_M = 1024 * 1024L;
                    ThrowUtils.throwIf(contentLength > 2 * ONE_M, ErrorCode.PARAMS_ERROR, "文件大小不能超过2M");
                }catch (NumberFormatException e){
                    log.error("file contentLengthStr parse error", e);
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小格式错误");
                }
            }

        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    /**
     * 删除临时文件
     */
    private void deleteTempFile(File file) {
        if (file == null) return;
        boolean delete = file.delete();
        if (!delete)
            log.error("file delete error,filepath:{}", file.getAbsolutePath());
    }
}
