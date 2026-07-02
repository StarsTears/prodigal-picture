package com.prodigal.system.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prodigal.system.annotation.PermissionCheck;
import com.prodigal.system.api.aliyunai.AliYunAiApi;
import com.prodigal.system.api.aliyunai.model.vo.CreateOutPaintingTaskVO;
import com.prodigal.system.api.aliyunai.model.vo.GetOutPaintingTaskVO;
import com.prodigal.system.api.imagesearch.CompositeImageSearchService;
import com.prodigal.system.api.imagesearch.ImageSearchDTO;
import com.prodigal.system.api.imagesearch.ImageSearchResult;
import com.prodigal.system.common.BaseResult;
import com.prodigal.system.common.ResultUtils;
import com.prodigal.system.constant.DictTypeConstant;
import com.prodigal.system.exception.BusinessException;
import com.prodigal.system.exception.ErrorCode;
import com.prodigal.system.exception.ThrowUtils;
import com.prodigal.system.manager.auth.SpaceUserAuthManager;
import com.prodigal.system.manager.auth.StpKit;
import com.prodigal.system.manager.auth.annotation.SaSpaceCheckPermission;
import com.prodigal.system.manager.auth.model.SpaceUserPermissionConstant;
import com.prodigal.system.model.dto.picture.*;
import com.prodigal.system.model.entity.Picture;
import com.prodigal.system.model.entity.Space;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.model.enums.PictureReviewStatusEnum;
import com.prodigal.system.model.vo.PictureTagCategory;
import com.prodigal.system.model.vo.PictureVO;
import com.prodigal.system.model.entity.Dict;
import com.prodigal.system.service.DictService;
import com.prodigal.system.service.PictureService;
import com.prodigal.system.service.SpaceService;
import com.prodigal.system.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
    @Resource
    private SpaceService spaceService;
    @Resource
    private AliYunAiApi aliYunAiApi;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;
    @Resource
    private DictService dictService;
    @Resource
    private CompositeImageSearchService compositeImageSearchService;

    /**
     * 图片上传
     *
     * @param multipartFile    文件
     * @param pictureUploadDto 接收请求参数
     * @param request          浏览器请求
     */
    @PostMapping("/upload")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_UPLOAD)
    public BaseResult<PictureVO> uploadPicture(@RequestPart MultipartFile multipartFile, PictureUploadDTO pictureUploadDto, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        // 若 DTO 无 requestId，从 Header 兜底
        if (StrUtil.isBlank(pictureUploadDto.getRequestId())) {
            pictureUploadDto.setRequestId(request.getHeader("X-Request-Id"));
        }
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadDto, loginUser);
        return ResultUtils.success(pictureVO);
    }

    @PostMapping("/upload/url")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_UPLOAD)
    public BaseResult<PictureVO> uploadPictureByUrl(@RequestBody PictureUploadDTO pictureUploadDto, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String fileUrl = pictureUploadDto.getFileUrl();
        PictureVO pictureVO = pictureService.uploadPicture(fileUrl, pictureUploadDto, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 抓取图片
     *
     * @param pictureUploadByBatchDto 接收请求参数
     * @param request                 浏览器请求
     */
    @PostMapping("/upload/batch")
    @PermissionCheck(mustRole = {"admin", "administrator"})
    public BaseResult<Integer> uploadPictureByBatch(@RequestBody PictureUploadByBatchDTO pictureUploadByBatchDto, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureUploadByBatchDto == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        int uploadCount = pictureService.uploadPictureByBatch(pictureUploadByBatchDto, loginUser);
        return ResultUtils.success(uploadCount);
    }

    /**
     * 获取图片临时下载地址（返回服务端代理下载 URL，不暴露 COS bucket/region）
     * @param pictureGetDto 图片查询参数
     * @return 下载 URL
     */
    @PostMapping("/get/download/url")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_VIEW)
    public BaseResult<String> getTempDownloadUrl(@RequestBody PictureGetDTO pictureGetDto) {
        ThrowUtils.throwIf(pictureGetDto == null || pictureGetDto.getId() == null, ErrorCode.PARAMS_ERROR);
        String tempDownloadUrl = pictureService.getTempDownloadUrl(pictureGetDto.getId(), pictureGetDto.getSpaceId());
        return ResultUtils.success(tempDownloadUrl);
    }

    /**
     * 以图搜图（Google + Bing 降级）
     *
     * @param imageSearchDto 图片搜索请求参数
     * @return List<ImageSearchResult>
     */
    @PostMapping("/search/picture")
    public BaseResult<List<ImageSearchResult>> searchImage(@RequestBody ImageSearchDTO imageSearchDto) {
        ThrowUtils.throwIf(imageSearchDto == null, ErrorCode.PARAMS_ERROR);
        String pictureId = imageSearchDto.getPictureId();
        ThrowUtils.throwIf(pictureId == null || StrUtil.isBlank(pictureId), ErrorCode.PARAMS_ERROR);

        String spaceId = imageSearchDto.getSpaceId() == null ? "0" : imageSearchDto.getSpaceId();
        // 构造 QueryWrapper
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", pictureId)         // 根据主键 id 查询
                .eq("space_id", spaceId); // 附加 spaceId 条件

        // 执行查询
        Picture oldPicture = pictureService.getOne(queryWrapper);

        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        //若该图有源图url则优先使用！反之则使用 COS 未压缩的图片Url
        String imageURL = StrUtil.isNotBlank(oldPicture.getOriginUrl()) ? oldPicture.getOriginUrl() : oldPicture.getUrl();
        List<ImageSearchResult> imageSearchResults = compositeImageSearchService.searchImage(imageURL);
        return ResultUtils.success(imageSearchResults);
    }

    /**
     * 图片删除
     *
     * @param pictureDeleteDto 接收请求参数
     * @param request          浏览器请求
     */
    @PostMapping("/delete")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_DELETE)
    public BaseResult<Boolean> deletePicture(@RequestBody PictureDeleteDTO pictureDeleteDto, HttpServletRequest request) {
        if (pictureDeleteDto == null || pictureDeleteDto.getId() == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        String spaceId = pictureDeleteDto.getSpaceId() == null ? "0" : pictureDeleteDto.getSpaceId();
        pictureService.deletePicture(pictureDeleteDto.getId(), spaceId, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 图片审核（管理员）
     *
     * @param pictureReviewDto 接收图片审核请求参数
     * @param request          浏览器请求
     */
    @PostMapping("/review")
    @PermissionCheck(mustRole = {"admin", "administrator"})
    public BaseResult<Boolean> doPictureReview(@Valid @RequestBody PictureReviewDTO pictureReviewDto, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureReviewDto == null, ErrorCode.PARAMS_ERROR);
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
    @PermissionCheck(mustRole = {"admin", "administrator"})
    public BaseResult<Boolean> updatePicture(@Valid @RequestBody PictureUpdateDTO pictureUpdateDto, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureUpdateDto == null, ErrorCode.PARAMS_ERROR);
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureUpdateDto, picture);
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateDto.getTags()));
        //数据校验
        pictureService.validPicture(picture);
        //判断图片是否存在
        String id = pictureUpdateDto.getId();
        String spaceId = pictureUpdateDto.getSpaceId() == null ? "0" : pictureUpdateDto.getSpaceId();
        // 构造 QueryWrapper
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id)         // 根据主键 id 查询
                .eq("space_id", spaceId); // 附加 spaceId 条件

        // 执行查询
        Picture oldPicture = pictureService.getOne(queryWrapper);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        //补充审核参数
        User loginUser = userService.getLoginUser(request);
        pictureService.fillReviewParams(picture, loginUser, spaceId);

        // 构造 UpdateWrapper
        UpdateWrapper<Picture> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", picture.getId()) // 指定主键条件，批量更新则使用 in 传递多条
                .eq("space_id", spaceId);      // 补充条件 spaceId=xxx

        // 执行更新
        boolean result = pictureService.update(picture, updateWrapper);
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
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResult<Boolean> editPicture(@Valid @RequestBody PictureEditDTO pictureEditDto, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureEditDto == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.editPicture(pictureEditDto, loginUser);

        return ResultUtils.success(true);
    }

    @PostMapping("/edit/batch")
    public BaseResult<Boolean> editPictureByBatch(@Valid @RequestBody PictureEditByBatchDTO pictureEditByBatchDto, HttpServletRequest request) {
        if (pictureEditByBatchDto == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        pictureService.editPictureByBatch(pictureEditByBatchDto, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 图片查询(管理员)
     *
     * @param pictureGetDto 接收请求参数
     * @param request       浏览器请求
     */
    @PostMapping("/get")
    @PermissionCheck(mustRole = {"administrator", "admin"})
    public BaseResult<Picture> getPictureByID(@RequestBody PictureGetDTO pictureGetDto, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureGetDto == null || pictureGetDto.getId() == null, ErrorCode.PARAMS_ERROR);
        String id = pictureGetDto.getId();
        String spaceId = pictureGetDto.getSpaceId() == null ? "0" : pictureGetDto.getSpaceId();
        // 构造 QueryWrapper
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id)         // 根据主键 id 查询
                .eq("space_id", spaceId); // 附加 spaceId 条件
        //执行查询
        Picture picture = pictureService.getOne(queryWrapper);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        //空间权限校验
        spaceId = picture.getSpaceId();
        if (!"0".equals(spaceId)) {
            User loginUser = userService.getLoginUser(request);
            pictureService.checkPicturePermission(loginUser, picture);
        }
        return ResultUtils.success(picture);
    }

    /**
     * 图片查询VO
     *
     * @param pictureGetDto 图片id
     * @param request       浏览器请求
     */
    @PostMapping("/get/vo")
    public BaseResult<PictureVO> getPictureVOByID(@RequestBody PictureGetDTO pictureGetDto,@RequestParam("isView") Boolean isView, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureGetDto == null || pictureGetDto.getId() == null, ErrorCode.PARAMS_ERROR);
        isView = isView==null?false:isView;
        String id = pictureGetDto.getId();
        String spaceId = pictureGetDto.getSpaceId() == null ? "0" : pictureGetDto.getSpaceId();
        // 构造 QueryWrapper
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id)         // 根据主键 id 查询
                .eq("space_id", spaceId); // 附加 spaceId 条件
        //执行查询
        Picture picture = pictureService.getOne(queryWrapper);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        Space space = null;
        spaceId = picture.getSpaceId();
        if (!"0".equals(spaceId)) {
            boolean hasPermission = StpKit.SPACE.hasPermission(SpaceUserPermissionConstant.PICTURE_VIEW);
            ThrowUtils.throwIf(!hasPermission, ErrorCode.USER_NOT_PERMISSION);
            space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        }
        //变更如下：将必须登录才可查看详情，改为无需登录也可查看；
        PictureVO pictureVO = pictureService.getPictureVO(picture, request);
        try {
            User loginUser = userService.getLoginUser(request);
            List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
            pictureVO.setPermissionList(permissionList);
            //是否为查看-查看次数 +1
            if (isView) {
                picture.setViewQuantity(picture.getViewQuantity() + 1);
                UpdateWrapper<Picture> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("id", id)         // 根据主键 id 查询
                        .eq("space_id", spaceId); // 附加 spaceId 条件
                // 执行更新
                boolean result = pictureService.update(picture, updateWrapper);
            }
        }catch (BusinessException e){
            if (e.getCode() == ErrorCode.USER_NOT_LOGIN.getCode()){
                return ResultUtils.success(pictureVO);
            }else {
                throw e;
            }
        }
        return ResultUtils.success(pictureVO);
    }

    /**
     * 图片分享次数+1
     */
    @PostMapping("/share")
    public BaseResult<Boolean> incrementShareQuantity(@RequestBody PictureGetDTO pictureGetDto) {
        ThrowUtils.throwIf(pictureGetDto == null || pictureGetDto.getId() == null, ErrorCode.PARAMS_ERROR);
        String id = pictureGetDto.getId();
        String spaceId = pictureGetDto.getSpaceId() == null ? "0" : pictureGetDto.getSpaceId();
        UpdateWrapper<Picture> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id).eq("space_id", spaceId)
                .setSql("share_quantity = share_quantity + 1");
        boolean result = pictureService.update(updateWrapper);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 图片分页查询
     *
     * @param pictureQueryDto 接收图片查询请求参数
     * @param request         浏览器请求
     */
    @PostMapping("/list/page")
    @PermissionCheck(mustRole = {"administrator", "admin"})
    public BaseResult<Page<Picture>> listPictureByPage(@RequestBody PictureQueryDTO pictureQueryDto, HttpServletRequest request) {
        long current = pictureQueryDto.getCurrent();
        long size = pictureQueryDto.getPageSize();
        String picColor = pictureQueryDto.getPicColor();
        if (StrUtil.isNotBlank(picColor)) {
            List<Picture> pictureList = pictureService.list(pictureService.getQueryWrapper(pictureQueryDto));
            Color targetColor = Color.decode(picColor);
            pictureList = pictureService.getPicturePageWithColor(targetColor, pictureList);
            long total = pictureList.size();
            Page<Picture> picturePage = new Page<>(current, size, total, false);
            int fromIndex = (int) ((current - 1) * size);
            int toIndex = (int) Math.min(current * size, total);
            picturePage.setRecords(pictureList.subList(fromIndex, toIndex));
            return ResultUtils.success(picturePage);
        }
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
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_VIEW)
    public BaseResult<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryDTO pictureQueryDto, HttpServletRequest request) {
        long current = pictureQueryDto.getCurrent();
        long size = pictureQueryDto.getPageSize() == 0 ? 20 : pictureQueryDto.getPageSize();
        pictureQueryDto.setReviewStatus(PictureReviewStatusEnum.PASS);
        ThrowUtils.throwIf(size > 30, ErrorCode.PARAMS_ERROR);
        String spaceId = pictureQueryDto.getSpaceId();
        if (spaceId == null) {
            pictureQueryDto.setReviewStatus(PictureReviewStatusEnum.PASS);
            pictureQueryDto.setNullSpaceId(true);
        } else {
            boolean hasPermission = StpKit.SPACE.hasPermission(SpaceUserPermissionConstant.PICTURE_VIEW);
            ThrowUtils.throwIf(!hasPermission, ErrorCode.USER_NOT_PERMISSION);
        }
        String picColor = pictureQueryDto.getPicColor();
        if (StrUtil.isNotBlank(picColor)) {
            List<Picture> pictureList = pictureService.list(pictureService.getQueryWrapper(pictureQueryDto));
            Color targetColor = Color.decode(picColor);
            pictureList = pictureService.getPicturePageWithColor(targetColor, pictureList);
            long total = pictureList.size();
//            Page<PictureVO> pictureVOPage = new Page<>(current, size, total, false);
            int fromIndex = (int) ((current - 1) * size);
            int toIndex = (int) Math.min(current * size, total);
            List<Picture> pageRecords = pictureList.subList(fromIndex, toIndex);
            Page<Picture> picturePage = new Page<>(current, size, total, false);
            picturePage.setRecords(pageRecords);
            return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));
        }
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryDto));
        Page<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage, request);
        return ResultUtils.success(pictureVOPage);
    }

    /**
     * 图片分页查询(VO-cache)
     *
     * @param pictureQueryDto 接收图片查询请求参数
     * @param request         浏览器请求
     */
    @PostMapping("/list/page/vo/cache")
    public BaseResult<Page<PictureVO>> listPictureVOByPageCache(@RequestBody PictureQueryDTO pictureQueryDto, HttpServletRequest request) {
        long size = pictureQueryDto.getPageSize();
        pictureQueryDto.setReviewStatus(PictureReviewStatusEnum.PASS);
        // 限制爬虫
        ThrowUtils.throwIf(size > 30, ErrorCode.PARAMS_ERROR);
        String spaceId = pictureQueryDto.getSpaceId();
        if (StrUtil.isBlank(spaceId)) {
            //普通用户查看公共图库(已过审)
            pictureQueryDto.setReviewStatus(PictureReviewStatusEnum.PASS);
            pictureQueryDto.setNullSpaceId(true);
        } else {
            User loginUser = userService.getLoginUser(request);
            Space space = spaceService.getById(spaceId);
            if (space == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            }
            ThrowUtils.throwIf(!space.getUserId().equals(loginUser.getId()), ErrorCode.USER_NOT_AUTHORIZED, "用户没有权限查看该空间图片");
        }
        Page<PictureVO> pictureVOPageCache = pictureService.getPictureVOPageCache(pictureQueryDto, request);

        return ResultUtils.success(pictureVOPageCache);
    }


    @GetMapping("/tag_category")
    public BaseResult<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = dictService.listByType(DictTypeConstant.PIC_TAG).stream()
                .map(Dict::getDictValue)
                .collect(Collectors.toList());
        List<String> categoryList = dictService.listByType(DictTypeConstant.PIC_CATEGORY).stream()
                .map(Dict::getDictValue)
                .collect(Collectors.toList());
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }


    /**
     * 创建AI 扩图任务
     *
     * @param createPictureOutPaintingTaskDto 扩图任务参数
     * @param request                         HttpRequest
     * @return 响应
     */
    @PostMapping("/out_painting/create_task")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResult<CreateOutPaintingTaskVO> createPictureOutPaintingTask
    (@Valid @RequestBody CreatePictureOutPaintingTaskDTO createPictureOutPaintingTaskDto, HttpServletRequest request) {
        ThrowUtils.throwIf(createPictureOutPaintingTaskDto == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        CreateOutPaintingTaskVO pictureOutPaintingTask = pictureService.createPictureOutPaintingTask(createPictureOutPaintingTaskDto, loginUser);
        return ResultUtils.success(pictureOutPaintingTask);
    }

    /**
     * 查询AI 扩图任务
     *
     * @param taskId 任务ID
     * @return 响应
     */
    @GetMapping("/out_painting/get_task")
    public BaseResult<GetOutPaintingTaskVO> getPictureOutPaintingTask(@RequestParam("taskId") String taskId) {
        ThrowUtils.throwIf(taskId == null, ErrorCode.PARAMS_ERROR);
        GetOutPaintingTaskVO outPaintingTask = aliYunAiApi.getOutPaintingTask(taskId);
        return ResultUtils.success(outPaintingTask);
    }

    @PostMapping("/test/sharding")
    public BaseResult<List<Picture>> testSharding() {

        //查询数据表中 已删除且最后修改时间是 7 天前的数据
        Date date = DateUtils.addWeeks(new Date(), -1);
        List<Picture> pictureList = pictureService.selectDeletedPictures(date);
        return ResultUtils.success(pictureList);
    }

}
