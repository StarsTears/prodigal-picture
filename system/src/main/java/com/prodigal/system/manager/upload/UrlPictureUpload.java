package com.prodigal.system.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.prodigal.system.exception.BusinessException;
import com.prodigal.system.exception.ErrorCode;
import com.prodigal.system.exception.ThrowUtils;
import com.prodigal.system.model.dto.file.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: Url上传图片服务
 **/
@Slf4j
@Service
public class UrlPictureUpload extends PictureUploadTemplate{
    @Override
    protected void verifyPicture(Object inputSource) {
        String fileUrl = (String) inputSource;
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
                final List<String> IMAGE_TYPE = Arrays.asList( "image/jpg","image/jpeg","image/webp","image/png");
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

    @Override
    protected String getOriginalFilename(Object inputSource) {
        String fileUrl = (String) inputSource;
        return String.format("%s.%s",FileUtil.mainName(fileUrl), FileUtil.getSuffix(fileUrl));
    }

    @Override
    protected void processFile(Object inputSource, File file) throws IOException {
        String fileUrl = (String) inputSource;
        HttpUtil.downloadFile(fileUrl, file);
    }

    @Override
    protected void processUploadResult(Object inputSource, UploadPictureResult uploadPictureResult) {
        String fileUrl = (String) inputSource;
        uploadPictureResult.setSourceUrl(fileUrl);
    }
}
