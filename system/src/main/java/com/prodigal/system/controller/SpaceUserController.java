package com.prodigal.system.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prodigal.system.annotation.PermissionCheck;
import com.prodigal.system.common.BaseResult;
import com.prodigal.system.common.DeleteRequest;
import com.prodigal.system.common.ResultUtils;
import com.prodigal.system.exception.BizStatus;
import com.prodigal.system.exception.ThrowUtils;
import com.prodigal.system.manager.auth.annotation.SaSpaceCheckPermission;
import com.prodigal.system.manager.auth.model.SpaceUserPermissionConstant;
import com.prodigal.system.model.dto.spaceuser.SpaceUserAddDTO;
import com.prodigal.system.model.dto.spaceuser.SpaceUserEditDTO;
import com.prodigal.system.model.dto.spaceuser.SpaceUserQueryDTO;
import com.prodigal.system.model.entity.SpaceUser;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.model.vo.SpaceUserVO;
import com.prodigal.system.service.SpaceUserService;
import com.prodigal.system.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
     * @param spaceUserAddDTO
     * @param request
     * @return
     */
    @PostMapping("/add")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResult<String> addSpaceUser(@Valid @RequestBody SpaceUserAddDTO spaceUserAddDTO, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserAddDTO == null, BizStatus.PARAMS_ERROR);
        String spaceId = spaceUserService.addSpaceUser(spaceUserAddDTO);

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
    public BaseResult<Boolean> deleteSpaceUser(@Valid @RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null, BizStatus.PARAMS_ERROR);
        String id = deleteRequest.getId();
        //判断是否存在
        SpaceUser oldSpaceUser = spaceUserService.getById(id);
        ThrowUtils.throwIf(oldSpaceUser == null, BizStatus.NOT_FOUND_ERROR);
        boolean result = spaceUserService.removeById(id);
        ThrowUtils.throwIf(!result, BizStatus.OPERATION_ERROR);
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
    public BaseResult<Boolean> editSpaceUser(@Valid @RequestBody SpaceUserEditDTO spaceUserEditDto, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserEditDto == null, BizStatus.PARAMS_ERROR);
        //将实体类 和 DTO 进行转换
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(spaceUserEditDto, spaceUser);
        //数据校验
        spaceUserService.validSpaceUser(spaceUser, false);
        //判断该数据是否存在
        SpaceUser oldSpaceUser = spaceUserService.getById(spaceUser.getId());
        ThrowUtils.throwIf(oldSpaceUser == null, BizStatus.NOT_FOUND_ERROR);
        boolean result = spaceUserService.updateById(spaceUser);
        ThrowUtils.throwIf(!result, BizStatus.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 获取某个成员在某个空间的信息
     * @param spaceUserQueryDTO
     * @param request
     * @return
     */
    @PostMapping("/get")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResult<SpaceUser> getSpaceUser(@RequestBody SpaceUserQueryDTO spaceUserQueryDTO, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserQueryDTO == null, BizStatus.PARAMS_ERROR);
        String spaceId = spaceUserQueryDTO.getSpaceId();
        String userId = spaceUserQueryDTO.getUserId();
        ThrowUtils.throwIf(ObjectUtil.hasEmpty(spaceId, userId), BizStatus.PARAMS_ERROR);

        SpaceUser spaceUser = spaceUserService.getOne(spaceUserService.getQueryWrapper(spaceUserQueryDTO));
        ThrowUtils.throwIf(spaceUser == null, BizStatus.NOT_FOUND_ERROR);
        return ResultUtils.success(spaceUser);
    }

    /**
     * 查询某个空间成员列表
     * @param spaceUserQueryDTO
     * @param request
     * @return
     */

    @PostMapping("/list/page")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    @PermissionCheck(mustRole = {"administrator", "admin"})
    public BaseResult<Page<SpaceUserVO>> listSpaceUserVOByPage(@RequestBody SpaceUserQueryDTO spaceUserQueryDTO, HttpServletRequest request) {
        long current = spaceUserQueryDTO.getCurrent();
        long size = spaceUserQueryDTO.getPageSize();

        Page<SpaceUser> spaceUserPage = spaceUserService.page(new Page<>(current, size), spaceUserService.getQueryWrapper(spaceUserQueryDTO));
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

        SpaceUserQueryDTO spaceUserQueryDTO = new SpaceUserQueryDTO();
        spaceUserQueryDTO.setUserId(loginUser.getId());

//        Page<SpaceUser> spaceUserPage = spaceUserService.page(new Page<>(current, size), spaceUserService.getQueryWrapper(spaceUserQueryDTO));
//        List<SpaceUserVO> spaceUserVOList = spaceUserService.getSpaceUserVOList(spaceUserPage.getRecords());
//
//        Page<SpaceUserVO> spaceUserVOPage = new Page<>(spaceUserPage.getCurrent(), spaceUserPage.getSize(), spaceUserPage.getTotal());
//        spaceUserVOPage.setRecords(spaceUserVOList);

        List<SpaceUser> spaceUserList = spaceUserService.list(spaceUserService.getQueryWrapper(spaceUserQueryDTO));

        return ResultUtils.success(spaceUserService.getSpaceUserVOList(spaceUserList));
    }

}
