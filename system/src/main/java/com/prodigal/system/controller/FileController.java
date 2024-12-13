package com.prodigal.system.controller;

import com.prodigal.system.annotation.PermissionCheck;
import com.prodigal.system.common.BaseResult;
import com.prodigal.system.common.ResultUtils;
import com.prodigal.system.constant.UserConstant;
import com.prodigal.system.manager.CosManager;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

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

    /**
     * 测试文件上传
     *
     * @param multipartFile 文件
     * @return 文件路径
     */
    @PostMapping("/test/upload")
    @PermissionCheck(mustRole = {UserConstant.SUPER_ADMIN_ROLE, UserConstant.ADMIN_ROLE})
    public BaseResult<String> testUploadFile(@RequestPart MultipartFile multipartFile) {
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
            log.error("file upload error,filepath:{}={}", filepath, e);
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
     * 测试下载文件
     * @param filepath 文件路径
     * @param response 响应对象
     */
    @GetMapping("/test/download")
    @PermissionCheck(mustRole = {UserConstant.SUPER_ADMIN_ROLE, UserConstant.ADMIN_ROLE})
    public void testDownloadFile(String filepath, HttpServletResponse response) throws IOException {
        COSObjectInputStream cosObjectInput = null;
        try {
            COSObject cosObject = cosManager.getObject(filepath);
            cosObjectInput = cosObject.getObjectContent();
            //处理下载到的流
            byte[] bytes = IOUtils.toByteArray(cosObjectInput);
            //设置响应头
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + filepath);
            //写入响应
            response.getOutputStream().write(bytes);
            //刷新输出流
            response.getOutputStream().flush();
        } catch (IOException e) {
            log.error("file download error,filepath:{}={}", filepath, e);
            throw new RuntimeException(e);
        }finally {
            //释放输出流
            if (null != cosObjectInput) {
                cosObjectInput.close();
            }
        }
    }
}
