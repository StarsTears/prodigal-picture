package com.prodigal.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prodigal.system.annotation.PermissionCheck;
import com.prodigal.system.common.BaseResult;
import com.prodigal.system.common.DeleteRequest;
import com.prodigal.system.common.ResultUtils;
import com.prodigal.system.constant.UserConstant;
import com.prodigal.system.exception.BusinessException;
import com.prodigal.system.exception.ErrorCode;
import com.prodigal.system.exception.ThrowUtils;
import com.prodigal.system.model.dto.picture.PictureEditDto;
import com.prodigal.system.model.dto.picture.PictureQueryDto;
import com.prodigal.system.model.dto.space.SpaceAddDto;
import com.prodigal.system.model.dto.space.SpaceEditDto;
import com.prodigal.system.model.dto.space.SpaceQueryDto;
import com.prodigal.system.model.dto.space.SpaceUpdateDto;
import com.prodigal.system.model.entity.Picture;
import com.prodigal.system.model.entity.Space;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.model.enums.SpaceLevelEnum;
import com.prodigal.system.model.vo.PictureVO;
import com.prodigal.system.model.vo.SpaceLevel;
import com.prodigal.system.model.vo.SpaceVO;
import com.prodigal.system.service.SpaceService;
import com.prodigal.system.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 空间控制层
 **/
@RestController
@RequestMapping("/space")
public class SpaceController {
    @Resource
    private SpaceService spaceService;
    @Resource
    private UserService userService;

    @PostMapping("/add")
    public BaseResult<Long> addSpace(@RequestBody SpaceAddDto spaceAddDto, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceAddDto == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        long spaceId = spaceService.addSpace(spaceAddDto, loginUser);

        return ResultUtils.success(spaceId);
    }
    @PostMapping("/update")
    @PermissionCheck(mustRole = {UserConstant.ADMIN_ROLE, UserConstant.SUPER_ADMIN_ROLE})
    public BaseResult<Boolean> updateSpace(@RequestBody SpaceUpdateDto spaceUpdateDto, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUpdateDto == null, ErrorCode.PARAMS_ERROR);
        Space space = new Space();
        BeanUtils.copyProperties(spaceUpdateDto, space);
        spaceService.fillSpaceBySpaceLevel(space);
        //校验
        spaceService.validSpace(space, false);
        //校验空间是否存在
        Long id = spaceUpdateDto.getId();
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        //操作数据库
        boolean result = spaceService.updateById(space);
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
    public BaseResult<Boolean> editPicture(@RequestBody SpaceEditDto spaceEditDto, HttpServletRequest request) {
        if (spaceEditDto == null || spaceEditDto.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        spaceService.editSpace(spaceEditDto, loginUser);
        return ResultUtils.success(true);
    }
    @PostMapping("/delete")
    public BaseResult<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        spaceService.deleteSpace(deleteRequest.getId(), loginUser);
        return ResultUtils.success(true);
    }

    @GetMapping("/get")
    @PermissionCheck(mustRole = {UserConstant.SUPER_ADMIN_ROLE, UserConstant.ADMIN_ROLE})
    public BaseResult<Space> getSpaceByID(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);

        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(space);
    }
    /**
     * 空间查询VO
     *
     * @param id      图片id
     * @param request 浏览器请求
     */
    @GetMapping("/get/vo")
    public BaseResult<SpaceVO> getSpaceVOByID(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Space Space = spaceService.getById(id);
        return ResultUtils.success(spaceService.getSpaceVO(Space, request));
    }

    @PostMapping("/list/page")
    @PermissionCheck(mustRole = {UserConstant.SUPER_ADMIN_ROLE, UserConstant.ADMIN_ROLE})
    public BaseResult<Page<Space>> listSpaceByPage(@RequestBody SpaceQueryDto spaceQueryDto, HttpServletRequest request) {
        long current = spaceQueryDto.getCurrent();
        long size = spaceQueryDto.getPageSize();
        Page<Space> spacePage = spaceService.page(new Page<>(current, size), spaceService.getQueryWrapper(spaceQueryDto));
        return ResultUtils.success(spacePage);
    }

    @PostMapping("/list/page/vo")
    public BaseResult<Page<SpaceVO>> listSpaceVOByPage(@RequestBody SpaceQueryDto spaceQueryDto, HttpServletRequest request) {
        long current = spaceQueryDto.getCurrent();
        long size = spaceQueryDto.getPageSize();
        Page<Space> spacePage = spaceService.page(new Page<>(current, size), spaceService.getQueryWrapper(spaceQueryDto));
        return ResultUtils.success(spaceService.listSpaceVOeByPage(spacePage, request));
    }
    /**
     * 获取空间级别
     * @return List<SpaceLevel>
     */
    @GetMapping("/list/level")
    public BaseResult<List<SpaceLevel>> listSpaceLevel() {
        List<SpaceLevel> collect = Arrays.stream(SpaceLevelEnum.values()).map(e -> new SpaceLevel(e.getValue(), e.getText(), e.getMaxCount(), e.getMaxSize())).collect(Collectors.toList());
        return ResultUtils.success(collect);
    }
}
