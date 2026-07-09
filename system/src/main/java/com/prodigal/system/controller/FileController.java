package com.prodigal.system.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.prodigal.system.annotation.PermissionCheck;
import com.prodigal.system.common.BaseResult;
import com.prodigal.system.common.ResultUtils;
import com.prodigal.system.exception.BizStatus;
import com.prodigal.system.exception.ThrowUtils;
import com.prodigal.system.manager.CosManager;
import com.prodigal.system.model.entity.Picture;
import com.prodigal.system.service.PictureService;
import com.qcloud.cos.model.ObjectMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 文件上传
 **/
@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {
    @Resource
    private CosManager cosManager;
    @Resource
    private PictureService pictureService;

    /**
     * 测试文件上传
     *
     * @param multipartFile 文件
     * @return 文件路径
     */
    @PostMapping("/test/upload")
    @PermissionCheck(mustRole = {"administrator", "admin"})
    public BaseResult<String> testUploadFile(@RequestPart("multipartFile") MultipartFile multipartFile) {
        //文件目录
        String filename = multipartFile.getOriginalFilename();
        String filepath = String.format("/test/%s", filename);
        File file = null;
        try {
            //创建临时文件
            file = File.createTempFile(filepath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filepath, file);
            //返回访问地址
            return ResultUtils.success(filepath);
        } catch (IOException e) {
            log.error("file upload error,filepath:{}", filepath, e);
            throw new RuntimeException(e);
        } finally {
            //删除临时文件
            if (null != file) {
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error,filepath:{}", filepath);
                }
            }
        }
    }

    /**
     * 测试下载文件（服务端流式代理，不加载整个文件到内存）
     * @param filepath 文件路径
     * @param response 响应对象
     */
    @GetMapping("/test/download")
    @PermissionCheck(mustRole = {"administrator", "admin"})
    public void testDownloadFile(String filepath, HttpServletResponse response) throws IOException {
        ThrowUtils.throwIf(StrUtil.isBlank(filepath), BizStatus.PARAMS_ERROR);
        streamFileToResponse(filepath, filepath.substring(filepath.lastIndexOf('/') + 1), response);
    }

    /**
     * 图片文件下载（通过 pictureId 查找 COS key，服务端流式代理下载）
     * @param pictureId 图片ID
     * @param spaceId 空间ID
     * @param response 响应对象
     */
    @GetMapping("/download")
    public void downloadFile(String pictureId, String spaceId, HttpServletResponse response) throws IOException {
        ThrowUtils.throwIf(pictureId == null || StrUtil.isBlank(pictureId), BizStatus.PARAMS_ERROR);
        String sid = spaceId == null ? "0" : spaceId;
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", pictureId).eq("space_id", sid);
        Picture picture = pictureService.getOne(queryWrapper);
        ThrowUtils.throwIf(picture == null, BizStatus.NOT_FOUND_ERROR, "图片不存在");

        String urlKey = StrUtil.isNotBlank(picture.getOriginUrl()) ? picture.getOriginUrl() : picture.getUrl();
        ThrowUtils.throwIf(StrUtil.isBlank(urlKey), BizStatus.NOT_FOUND_ERROR, "图片路径为空");

        String fileName = StrUtil.isNotBlank(picture.getName()) ? picture.getName()
                : urlKey.substring(urlKey.lastIndexOf('/') + 1);
        streamFileToResponse(urlKey, fileName, response);
    }

    private void streamFileToResponse(String cosKey, String fileName, HttpServletResponse response) throws IOException {
        try {
            ObjectMetadata metadata = cosManager.getObjectMetadata(cosKey);
            String contentType = metadata.getContentType();
            if (StrUtil.isBlank(contentType)) {
                contentType = "application/octet-stream";
            }
            response.setContentType(contentType);
            response.setContentLengthLong(metadata.getContentLength());
            response.setHeader("Content-Disposition",
                    "attachment; filename*=UTF-8''" + URLEncoder.encode(fileName, "UTF-8").replace("+", "%20"));
            cosManager.streamToOutput(cosKey, response.getOutputStream());
        } catch (IOException e) {
            log.error("file download error, key:{}", cosKey, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 测试获取文件临时下载地址
     * @param filepath 图片路径
     * @return 临时下载地址
     */
    @GetMapping("/test/get/temp/url")
    @PermissionCheck(mustRole = {"administrator", "admin"})
    public BaseResult<String> testGetTempURL(@RequestParam("filepath") String filepath) {
        ThrowUtils.throwIf(StrUtil.isBlank(filepath), BizStatus.PARAMS_ERROR);
        String tempUrl = cosManager.generateTempUrl(filepath);
        return ResultUtils.success(tempUrl);
    }
}
