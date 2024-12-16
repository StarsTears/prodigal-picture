package com.prodigal.system.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prodigal.system.annotation.PermissionCheck;
import com.prodigal.system.common.BaseResult;
import com.prodigal.system.common.DeleteRequest;
import com.prodigal.system.common.ResultUtils;
import com.prodigal.system.constant.UserConstant;
import com.prodigal.system.exception.BusinessException;
import com.prodigal.system.exception.ErrorCode;
import com.prodigal.system.exception.ThrowUtils;
import com.prodigal.system.model.dto.picture.*;
import com.prodigal.system.model.entity.Picture;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.model.enums.PictureReviewStatusEnum;
import com.prodigal.system.model.vo.PictureTagCategory;
import com.prodigal.system.model.vo.PictureVO;
import com.prodigal.system.service.PictureService;
import com.prodigal.system.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 图片控制器
 **/
@RestController
@RequestMapping("/picture")
public class PictureController {
    @Resource
    private PictureService pictureService;
    @Resource
    private UserService userService;

    /**
     * 图片上传
     *
     * @param multipartFile    文件
     * @param pictureUploadDto 接收请求参数
     * @param request          浏览器请求
     */
    @PostMapping("/upload")
//    @PermissionCheck(mustRole = {UserConstant.SUPER_ADMIN_ROLE, UserConstant.ADMIN_ROLE})
    public BaseResult<PictureVO> uploadPicture(@RequestPart MultipartFile multipartFile, PictureUploadDto pictureUploadDto, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadDto, loginUser);
        return ResultUtils.success(pictureVO);
    }
    @PostMapping("/upload/url")
    public BaseResult<PictureVO> uploadPictureByUrl(@RequestBody PictureUploadDto pictureUploadDto, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String fileUrl = pictureUploadDto.getFileUrl();
        PictureVO pictureVO = pictureService.uploadPicture(fileUrl, pictureUploadDto, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 抓取图片
     * @param pictureUploadByBatchDto 接收请求参数
     * @param request 浏览器请求
     */
    @PostMapping("/upload/batch")
    @PermissionCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResult<Integer> uploadPictureByBatch(@RequestBody PictureUploadByBatchDto pictureUploadByBatchDto, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureUploadByBatchDto == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        int uploadCount = pictureService.uploadPictureByBatch(pictureUploadByBatchDto, loginUser);
        return ResultUtils.success(uploadCount);
    }

    /**
     * 图片删除
     *
     * @param deleteRequest 接收请求参数
     * @param request       浏览器请求
     */
    @PostMapping("/delete")
    public BaseResult<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //图片应只能管理员/本人删除
        User loginUser = userService.getLoginUser(request);
        Long id = deleteRequest.getId();
        //查询该图片是否存在
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        //验证权限
        if (!loginUser.getId().equals(picture.getUserId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.USER_NOT_PERMISSION);
        }
        boolean result = pictureService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 图片审核（管理员）
     * @param pictureReviewDto 接收图片审核请求参数
     * @param request 浏览器请求
     */
    @PostMapping("/review")
    @PermissionCheck(mustRole = {UserConstant.ADMIN_ROLE, UserConstant.SUPER_ADMIN_ROLE})
    public BaseResult<Boolean> doPictureReview(@RequestBody PictureReviewDto pictureReviewDto, HttpServletRequest request) {
        //1、参数校验
        ThrowUtils.throwIf(pictureReviewDto == null || pictureReviewDto.getId() <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.doPictureReview(pictureReviewDto, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 更新图片(管理员)
     *
     * @param pictureUpdateDto 接收图片更新请求参数
     * @param request          浏览器请求
     */
    @PostMapping("/update")
    @PermissionCheck(mustRole = {UserConstant.ADMIN_ROLE, UserConstant.SUPER_ADMIN_ROLE})
    public BaseResult<Boolean> updatePicture(@RequestBody PictureUpdateDto pictureUpdateDto, HttpServletRequest request) {
        if (pictureUpdateDto == null || pictureUpdateDto.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureUpdateDto, picture);
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateDto.getTags()));
        //数据校验
        pictureService.validPicture(picture);
        //判断图片是否存在
        Long id = pictureUpdateDto.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        //补充审核参数
        User loginUser = userService.getLoginUser(request);
        pictureService.fillReviewParams(picture, loginUser);

        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 图片编辑（用户使用）
     *
     * @param pictureEditDto 接收图片编辑请求参数
     * @param request        浏览器请求
     */
    @PostMapping("/edit")
    @PermissionCheck(mustRole = {UserConstant.ADMIN_ROLE, UserConstant.SUPER_ADMIN_ROLE})
    public BaseResult<Boolean> editPicture(@RequestBody PictureEditDto pictureEditDto, HttpServletRequest request) {
        if (pictureEditDto == null || pictureEditDto.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditDto, picture);
        picture.setTags(JSONUtil.toJsonStr(pictureEditDto.getTags()));
        //设置编辑时间
        picture.setEditTime(new Date());
        //数据校验
        pictureService.validPicture(picture);
        //判断图片是否存在
        Long id = pictureEditDto.getId();
        Picture oldPicture = pictureService.getById(id);
        //验证权限
        //图片应只能管理员/本人删除
        User loginUser = userService.getLoginUser(request);
        if (!loginUser.getId().equals(picture.getUserId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.USER_NOT_PERMISSION);
        }
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        //补充审核参数
        pictureService.fillReviewParams(picture, loginUser);
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 图片查询(管理员)
     *
     * @param id      接收请求参数
     * @param request 浏览器请求
     */
    @GetMapping("/get")
    @PermissionCheck(mustRole = {UserConstant.SUPER_ADMIN_ROLE, UserConstant.ADMIN_ROLE})
    public BaseResult<Picture> getPictureByID(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);

        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);

        return ResultUtils.success(picture);
    }

    /**
     * 图片查询VO
     *
     * @param id      图片id
     * @param request 浏览器请求
     */
    @GetMapping("/get/vo")
    public BaseResult<PictureVO> getPictureVO(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);

        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);

        return ResultUtils.success(pictureService.getPictureVO(picture, request));
    }

    /**
     * 图片分页查询
     *
     * @param pictureQueryDto 接收图片查询请求参数
     * @param request         浏览器请求
     */
    @PostMapping("/list/page")
    @PermissionCheck(mustRole = {UserConstant.SUPER_ADMIN_ROLE, UserConstant.ADMIN_ROLE})
    public BaseResult<Page<Picture>> listPictureByPage(@RequestBody PictureQueryDto pictureQueryDto, HttpServletRequest request) {
        long current = pictureQueryDto.getCurrent();
        long size = pictureQueryDto.getPageSize();
        // 限制爬虫
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size), pictureService.getQueryWrapper(pictureQueryDto));
        return ResultUtils.success(picturePage);
    }

    /**
     * 图片分页查询(VO)
     *
     * @param pictureQueryDto 接收图片查询请求参数
     * @param request         浏览器请求
     */
    @PostMapping("/list/page/vo")
    public BaseResult<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryDto pictureQueryDto, HttpServletRequest request) {
        long current = pictureQueryDto.getCurrent();
        long size = pictureQueryDto.getPageSize();
        pictureQueryDto.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size), pictureService.getQueryWrapper(pictureQueryDto));
        return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));
    }

    /**
     * TODO:数据量不大，可以暂时预设 [后面数据量大了，再改为从注册中心获取]
     */
    @GetMapping("/tag_category")
    public BaseResult<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }

}
