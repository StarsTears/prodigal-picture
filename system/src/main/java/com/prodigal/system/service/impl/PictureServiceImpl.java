package com.prodigal.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.prodigal.system.constant.FilePathConstant;
import com.prodigal.system.exception.ErrorCode;
import com.prodigal.system.exception.ThrowUtils;
import com.prodigal.system.manager.FileManager;
import com.prodigal.system.model.dto.file.UploadPictureResult;
import com.prodigal.system.model.dto.picture.PictureQueryDto;
import com.prodigal.system.model.dto.picture.PictureUploadDto;
import com.prodigal.system.model.entity.Picture;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.model.vo.PictureVO;
import com.prodigal.system.model.vo.UserVO;
import com.prodigal.system.service.PictureService;
import com.prodigal.system.mapper.PictureMapper;
import com.prodigal.system.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Lang
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2024-12-12 15:47:24
 */
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {
    @Resource
    private FileManager fileManager;
    @Resource
    private UserService userService;

    /**
     * 图片校验(更新与修改)
     */
    @Override
    public void validPicture(Picture picture){
        ThrowUtils.throwIf(picture==null, ErrorCode.PARAMS_ERROR);
        //更新或修改时，图片id不能为空
        ThrowUtils.throwIf(ObjUtil.isEmpty(picture.getId()), ErrorCode.PARAMS_ERROR,"id 不能为空！");
        //限制图片地址长度、简介
        ThrowUtils.throwIf(StrUtil.isNotBlank(picture.getUrl()) && picture.getUrl().length()>1024,
                ErrorCode.PARAMS_ERROR,"图片地址过长！");
        ThrowUtils.throwIf(StrUtil.isNotBlank(picture.getIntroduction()) && picture.getIntroduction().length()>1000,
                ErrorCode.PARAMS_ERROR,"图片简介过长！");
    }

    /**
     * 上传图片
     *
     * @param multipartFile    文件
     * @param pictureUploadDto 图片请求参数
     * @param loginUser        登录用户
     */
    @Override
    public PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadDto pictureUploadDto, User loginUser) {
        ThrowUtils.throwIf(loginUser == null, ErrorCode.USER_NOT_AUTHORIZED);
        //判断新增还是修改
        Long pictureID = null;
        if (pictureUploadDto != null) {
            pictureID = pictureUploadDto.getId();
        }
        //校验图片是否存在
        if (pictureID != null) {
            // Picture picture = this.getById(pictureID);
            boolean exists = this.lambdaQuery().eq(Picture::getId, pictureID).exists();
            ThrowUtils.throwIf(!exists, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        }
        //上传图片-根据用户id来划分目录
        String uploadPrefix = String.format("%s/%S", FilePathConstant.PICTURE_PUBLIC_PREFIX, loginUser.getId());
        UploadPictureResult uploadPictureResult = fileManager.uploadPicture(uploadPrefix, multipartFile);
        //构造需要存储的图片信息
        Picture picture = new Picture();
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setName(uploadPictureResult.getPicName());
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());

        //如果pictureID 不为空则进行更新
        if (pictureID != null) {
            picture.setId(pictureID);
            picture.setEditTime(new Date());
        }
        boolean result = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败");
        return PictureVO.objToVO(picture);
    }

    /**
     * 分页查询参数封装
     * @param pictureQueryDto
     * @return
     */
    @Override
    public LambdaQueryWrapper<Picture> getQueryWrapper(PictureQueryDto pictureQueryDto) {
        LambdaQueryWrapper<Picture> wrapper = new LambdaQueryWrapper<Picture>();
        if (pictureQueryDto == null) {
            return wrapper;
        }
        String sortOrder = pictureQueryDto.getSortOrder();
        String sortField = pictureQueryDto.getSortField() == null ? "" : pictureQueryDto.getSortField().trim();
        //关键字查询(名称，简介)
        if (StrUtil.isNotBlank(pictureQueryDto.getSearchText())) {
            wrapper.and(e -> e.like(Picture::getName, pictureQueryDto.getSearchText())
                    .or().like(Picture::getIntroduction, pictureQueryDto.getSearchText()));
        }
        wrapper.eq(ObjUtil.isNotEmpty(pictureQueryDto.getId()), Picture::getId, pictureQueryDto.getId())
                .eq(ObjUtil.isNotEmpty(pictureQueryDto.getUserId()), Picture::getUserId, pictureQueryDto.getUserId())
                .like(StrUtil.isNotBlank(pictureQueryDto.getName()), Picture::getName, pictureQueryDto.getName())
                .like(StrUtil.isNotBlank(pictureQueryDto.getIntroduction()), Picture::getIntroduction, pictureQueryDto.getIntroduction())
                .like(StrUtil.isNotBlank(pictureQueryDto.getPicFormat()), Picture::getPicFormat, pictureQueryDto.getPicFormat())
                .eq(StrUtil.isNotBlank(pictureQueryDto.getCategory()), Picture::getCategory, pictureQueryDto.getCategory())
                .eq(ObjUtil.isNotEmpty(pictureQueryDto.getPicHeight()), Picture::getPicHeight, pictureQueryDto.getPicHeight())
                .eq(ObjUtil.isNotEmpty(pictureQueryDto.getPicWidth()), Picture::getPicWidth, pictureQueryDto.getPicWidth())
                .eq(ObjUtil.isNotEmpty(pictureQueryDto.getPicSize()), Picture::getPicSize, pictureQueryDto.getPicSize())
                .eq(ObjUtil.isNotEmpty(pictureQueryDto.getPicScale()), Picture::getPicScale, pictureQueryDto.getPicScale());
        if (CollUtil.isNotEmpty(pictureQueryDto.getTags())) {
            // and (tags like "%"\tag1\"%" and tags like "%"\tag2\"%")
            for (String tag : pictureQueryDto.getTags()) {
                wrapper.like(Picture::getTags, "\""+tag+"\"");
            }
        }

        switch (sortField) {
            case "name":
                wrapper.orderBy(StrUtil.isNotEmpty(pictureQueryDto.getSortField()), sortOrder.equals("ascend"), Picture::getName);
                break;
            case "category":
                wrapper.orderBy(StrUtil.isNotEmpty(pictureQueryDto.getSortField()), sortOrder.equals("ascend"), Picture::getCategory);
                break;
            case "picHeight":
                wrapper.orderBy(StrUtil.isNotEmpty(pictureQueryDto.getSortField()), sortOrder.equals("ascend"), Picture::getPicHeight);
                break;
            case "picWidth":
                wrapper.orderBy(StrUtil.isNotEmpty(pictureQueryDto.getSortField()), sortOrder.equals("ascend"), Picture::getPicWidth);
                break;
            case "picSize":
                wrapper.orderBy(StrUtil.isNotEmpty(pictureQueryDto.getSortField()), sortOrder.equals("ascend"), Picture::getPicSize);
                break;
            case "picScale":
                wrapper.orderBy(StrUtil.isNotEmpty(pictureQueryDto.getSortField()), sortOrder.equals("ascend"), Picture::getPicScale);
                break;
            case "picFormat":
                wrapper.orderBy(StrUtil.isNotEmpty(pictureQueryDto.getSortField()), sortOrder.equals("ascend"), Picture::getPicFormat);
                break;
            case "createTime":
                wrapper.orderBy(StrUtil.isNotEmpty(pictureQueryDto.getSortField()),sortOrder.equals("ascend"),Picture::getCreateTime);
            case "editTime":
                wrapper.orderBy(StrUtil.isNotEmpty(pictureQueryDto.getSortField()),sortOrder.equals("ascend"),Picture::getEditTime);
                break;
            default:
                break;
        }
        return wrapper;
    }

    /**
     * 单张图片信息(VO封装)
     * @param picture 实体对象
     * @param request 浏览器请求
     */
    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request){
        //根据Picture信息 脱敏后返回给前端
        PictureVO pictureVO = PictureVO.objToVO(picture);
        //根据userID 获取UserVO
        Long userId = picture.getUserId();
        if (userId!=null && userId>0) {
            UserVO userVO = userService.getUserVO(userService.getById(userId));
            pictureVO.setUser(userVO);
        }
        return pictureVO;
    }

    /**
     * 分页图片查询(VO封装)
     * @param picturePage
     * @param request
     */
    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request){
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)){
            return pictureVOPage;
        }
        //转换VO
        List<PictureVO> pictureVOList = pictureList.stream().map(PictureVO::objToVO).collect(Collectors.toList());
        //获取UserVO
        Set<Long> userIDSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIDUserListMap = userService.listByIds(userIDSet).stream().collect(Collectors.groupingBy(User::getId));
        pictureVOList.forEach(e->{
            Long userId = e.getUserId();
            if (userIDUserListMap.containsKey(userId))
                e.setUser(userService.getUserVO(userIDUserListMap.get(userId).get(0)));
        });
        pictureVOPage.setRecords(pictureVOList);
       return pictureVOPage;
    }


}




