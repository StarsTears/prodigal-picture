package com.prodigal.system.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prodigal.system.annotation.PermissionCheck;
import com.prodigal.system.common.BaseResult;
import com.prodigal.system.common.DeleteRequest;
import com.prodigal.system.common.ResultUtils;
import com.prodigal.system.constant.UserConstant;
import com.prodigal.system.exception.BusinessException;
import com.prodigal.system.exception.ErrorCode;
import com.prodigal.system.exception.ThrowUtils;
import com.prodigal.system.manager.auth.annotation.SaSpaceCheckPermission;
import com.prodigal.system.manager.auth.model.SpaceUserPermissionConstant;
import com.prodigal.system.model.dto.spaceuser.SpaceUserAddDto;
import com.prodigal.system.model.dto.spaceuser.SpaceUserEditDto;
import com.prodigal.system.model.dto.spaceuser.SpaceUserQueryDto;
import com.prodigal.system.model.entity.SpaceUser;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.model.vo.SpaceUserVO;
import com.prodigal.system.service.SpaceUserService;
import com.prodigal.system.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 空间成员控制层
 **/
@RestController
@RequestMapping("/spaceUser")
public class SpaceUserController {
    @Resource
    private SpaceUserService spaceUserService;
    @Resource
    private UserService userService;

    /**
     * 添加成员到团队空间
     * @param spaceUserAddDto
     * @param request
     * @return
     */
    @PostMapping("/add")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResult<Long> addSpaceUser(@RequestBody SpaceUserAddDto spaceUserAddDto, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserAddDto == null, ErrorCode.PARAMS_ERROR);
        long spaceId = spaceUserService.addSpaceUser(spaceUserAddDto);

        return ResultUtils.success(spaceId);
    }

    /**
     * 从团队空间移除成员
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResult<Boolean> deleteSpaceUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        Long id = deleteRequest.getId();
        //判断是否存在
        SpaceUser oldSpaceUser = spaceUserService.getById(id);
        ThrowUtils.throwIf(oldSpaceUser == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = spaceUserService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }
    /**
     * 空间成员编辑(设置成员权限)
     *
     * @param spaceUserEditDto 接收空间成员编辑请求参数
     * @param request        浏览器请求
     */
    @PostMapping("/edit")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResult<Boolean> editSpaceUser(@RequestBody SpaceUserEditDto spaceUserEditDto, HttpServletRequest request) {
        if (spaceUserEditDto == null || spaceUserEditDto.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //将实体类 和 DTO 进行转换
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(spaceUserEditDto, spaceUser);
        //数据校验
        spaceUserService.validSpaceUser(spaceUser, false);
        //判断该数据是否存在
        SpaceUser oldSpaceUser = spaceUserService.getById(spaceUser.getId());
        ThrowUtils.throwIf(oldSpaceUser == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = spaceUserService.updateById(spaceUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 获取某个成员在某个空间的信息
     * @param spaceUserQueryDto
     * @param request
     * @return
     */
    @GetMapping("/get")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResult<SpaceUser> getSpaceUser(@RequestBody SpaceUserQueryDto spaceUserQueryDto, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserQueryDto == null, ErrorCode.PARAMS_ERROR);
        Long spaceId = spaceUserQueryDto.getSpaceId();
        Long userId = spaceUserQueryDto.getUserId();
        ThrowUtils.throwIf(ObjectUtil.hasEmpty(spaceId, userId), ErrorCode.PARAMS_ERROR);

        SpaceUser spaceUser = spaceUserService.getOne(spaceUserService.getQueryWrapper(spaceUserQueryDto));
        ThrowUtils.throwIf(spaceUser == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(spaceUser);
    }

    /**
     * 查询某个空间成员列表
     * @param spaceUserQueryDto
     * @param request
     * @return
     */

    @PostMapping("/list/page")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    @PermissionCheck(mustRole = {UserConstant.SUPER_ADMIN_ROLE, UserConstant.ADMIN_ROLE})
    public BaseResult<Page<SpaceUserVO>> listSpaceUserVOByPage(@RequestBody SpaceUserQueryDto spaceUserQueryDto, HttpServletRequest request) {
        long current = spaceUserQueryDto.getCurrent();
        long size = spaceUserQueryDto.getPageSize();

        Page<SpaceUser> spaceUserPage = spaceUserService.page(new Page<>(current, size), spaceUserService.getQueryWrapper(spaceUserQueryDto));
        List<SpaceUserVO> spaceUserVOList = spaceUserService.getSpaceUserVOList(spaceUserPage.getRecords());

        Page<SpaceUserVO> spaceUserVOPage = new Page<>(spaceUserPage.getCurrent(), spaceUserPage.getSize(), spaceUserPage.getTotal());
        spaceUserVOPage.setRecords(spaceUserVOList);

        return ResultUtils.success(spaceUserVOPage);
    }

    /**
     * 查询我加入的团队空间列表
     * @param request
     * @return
     */
    @PostMapping("/list/my")
    public BaseResult<List<SpaceUserVO>> listMyTeamSpace(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);

        SpaceUserQueryDto spaceUserQueryDto = new SpaceUserQueryDto();
        spaceUserQueryDto.setUserId(loginUser.getId());

//        Page<SpaceUser> spaceUserPage = spaceUserService.page(new Page<>(current, size), spaceUserService.getQueryWrapper(spaceUserQueryDto));
//        List<SpaceUserVO> spaceUserVOList = spaceUserService.getSpaceUserVOList(spaceUserPage.getRecords());
//
//        Page<SpaceUserVO> spaceUserVOPage = new Page<>(spaceUserPage.getCurrent(), spaceUserPage.getSize(), spaceUserPage.getTotal());
//        spaceUserVOPage.setRecords(spaceUserVOList);

        List<SpaceUser> spaceUserList = spaceUserService.list(spaceUserService.getQueryWrapper(spaceUserQueryDto));

        return ResultUtils.success(spaceUserService.getSpaceUserVOList(spaceUserList));
    }

}
