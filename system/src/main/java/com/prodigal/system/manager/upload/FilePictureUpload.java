package com.prodigal.system.manager.upload;

import cn.hutool.core.io.FileUtil;
import com.prodigal.system.exception.BusinessException;
import com.prodigal.system.exception.BizStatus;
import com.prodigal.system.exception.ThrowUtils;
import com.prodigal.system.model.dto.file.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 图片上传服务
 **/
@Slf4j
@Service
public class FilePictureUpload extends PictureUploadTemplate{

    @Override
    protected void verifyPicture(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        ThrowUtils.throwIf(multipartFile == null, BizStatus.PARAMS_ERROR, "文件不能为空");
        //校验文件大小
        long fileSize = multipartFile.getSize();
        final long ONE_M = 1024 * 1024;
        ThrowUtils.throwIf(fileSize > 10 * ONE_M, BizStatus.PARAMS_ERROR, "文件大小不能超过10M");
        //校验文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpg", "jpeg", "png", "webp");
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(fileSuffix), BizStatus.PARAMS_ERROR, "文件格式错误");
        //校验文件真实类型（magic bytes）
        try {
            String contentType = multipartFile.getContentType();
            List<String> ALLOW_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/png", "image/webp");
            ThrowUtils.throwIf(contentType == null || !ALLOW_CONTENT_TYPES.contains(contentType),
                    BizStatus.PARAMS_ERROR, "文件类型不合法");
        } catch (Exception e) {
            throw new BusinessException(BizStatus.PARAMS_ERROR, "文件校验失败");
        }
    }

    @Override
    protected String getOriginalFilename(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        return multipartFile.getOriginalFilename();
    }

    @Override
    protected void processFile(Object inputSource, File file) throws IOException {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        multipartFile.transferTo(file);
    }

    @Override
    protected void processUploadResult(Object inputSource, UploadPictureResult uploadPictureResult) {

    }
}
