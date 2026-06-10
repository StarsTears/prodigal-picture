package com.prodigal.system.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prodigal.system.annotation.PermissionCheck;
import com.prodigal.system.common.BaseResult;
import com.prodigal.system.common.DeleteRequest;
import com.prodigal.system.common.ResultUtils;
import com.prodigal.system.constant.UserConstant;
import com.prodigal.system.exception.BusinessException;
import com.prodigal.system.exception.ErrorCode;
import com.prodigal.system.exception.ThrowUtils;
import com.prodigal.system.model.dto.user.ChangePasswordDTO;
import com.prodigal.system.model.dto.user.UserAddDTO;
import com.prodigal.system.model.dto.user.UserQueryDTO;
import com.prodigal.system.model.dto.user.UserUpdateDTO;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.model.vo.UserVO;
import com.prodigal.system.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: user 模块
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    @PostMapping("/add")
    @PermissionCheck(mustRole = {UserConstant.ADMIN_ROLE, UserConstant.SUPER_ADMIN_ROLE})
    public BaseResult<String> addUser(@Valid @RequestBody UserAddDTO userAddDto) {
        return ResultUtils.success(userService.createUser(userAddDto));
    }

    /**
     * 根据ID获取用户（管理员）
     *
     * @param id 用户ID
     * @return 用户信息（脱敏）
     */
    @GetMapping("/get")
    @PermissionCheck(mustRole = {UserConstant.SUPER_ADMIN_ROLE})
    public BaseResult<User> getUserByID(@RequestParam("id") String id) {
        ThrowUtils.throwIf(StrUtil.isBlank(id), ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.USER_NOT_FOUND);
        return ResultUtils.success(user);
    }

    @GetMapping("/get/vo")
    @PermissionCheck(mustRole = {UserConstant.SUPER_ADMIN_ROLE, UserConstant.ADMIN_ROLE})
    public BaseResult<UserVO> getUserVOByID(@RequestParam("id") String id) {
        BaseResult<User> res = getUserByID(id);
        User user = res.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/delete")
    @PermissionCheck(mustRole = UserConstant.SUPER_ADMIN_ROLE)
    public BaseResult<Boolean> deleteUser(@Valid @RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR);
        User targetUser = userService.getById(deleteRequest.getId());
        if (targetUser != null && UserConstant.SUPER_ADMIN_ROLE.equals(targetUser.getUserRole())) {
            throw new BusinessException(ErrorCode.USER_NOT_PERMISSION);
        }
        boolean removeById = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(removeById);
    }

    /**
     * 更新用户信息(管理员)
     */
    @PostMapping("/update")
    @PermissionCheck(mustRole = {UserConstant.ADMIN_ROLE, UserConstant.SUPER_ADMIN_ROLE})
    public BaseResult<Boolean> updateUser(@Valid @RequestBody UserUpdateDTO userUpdateDto) {
        ThrowUtils.throwIf(userUpdateDto == null, ErrorCode.PARAMS_ERROR);
        // 不允许修改超级管理员的角色
        User targetUser = userService.getById(userUpdateDto.getId());
        if (targetUser != null && UserConstant.SUPER_ADMIN_ROLE.equals(targetUser.getUserRole())) {
            userUpdateDto.setUserRole(null);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateDto, user);
        boolean update = userService.updateById(user);
        ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(update);
    }

    /**
     * 更新用户信息(管理员和本人)
     */
    @PostMapping("/edit")
    public BaseResult<Boolean> editUser(@Valid @RequestBody UserUpdateDTO userUpdateDto, HttpServletRequest request) {
        ThrowUtils.throwIf(userUpdateDto == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        User targetUser = userService.getById(userUpdateDto.getId());
        boolean isTargetSuperAdmin = targetUser != null && UserConstant.SUPER_ADMIN_ROLE.equals(targetUser.getUserRole());
        // 超级管理员：只有本人能修改自己的基础信息
        if (isTargetSuperAdmin && !loginUser.getId().equals(targetUser.getId())) {
            throw new BusinessException(ErrorCode.USER_NOT_PERMISSION);
        }
        if (!loginUser.getId().equals(userUpdateDto.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.USER_NOT_PERMISSION);
        }
        // 非管理员或目标为超级管理员时，不允许修改角色
        if (!userService.isAdmin(loginUser) || isTargetSuperAdmin) {
            userUpdateDto.setUserRole(null);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateDto, user);
        boolean update = userService.updateById(user);
        ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(update);
    }

    /**
     * 登录用户修改密码（需邮箱验证码）
     */
    @PostMapping("/change-password")
    public BaseResult<Boolean> changePassword(@Valid @RequestBody ChangePasswordDTO dto, HttpServletRequest request) {
        ThrowUtils.throwIf(dto == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        userService.changePassword(dto, loginUser);
        return ResultUtils.success(true);
    }

    @PostMapping("/list/page/vo")
    @PermissionCheck(mustRole = {UserConstant.ADMIN_ROLE, UserConstant.SUPER_ADMIN_ROLE})
    public BaseResult<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryDTO userQueryDto) {
        ThrowUtils.throwIf(userQueryDto == null, ErrorCode.PARAMS_ERROR);
        long current = userQueryDto.getCurrent();
        long pageSize = userQueryDto.getPageSize();
        Page<User> page = userService.page(new Page<>(current, pageSize),
                userService.getQueryWrapper(userQueryDto));
        Page<UserVO> userVOPage = new Page<>(current, pageSize, page.getTotal());
        List<UserVO> userVOList = userService.getUserVOList(page.getRecords());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }
}
