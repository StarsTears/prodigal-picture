package com.prodigal.system.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prodigal.system.annotation.PermissionCheck;
import com.prodigal.system.common.BaseResult;
import com.prodigal.system.common.DeleteRequest;
import com.prodigal.system.common.ResultUtils;
import com.prodigal.system.constant.UserConstant;
import com.prodigal.system.exception.BusinessException;
import com.prodigal.system.exception.ErrorCode;
import com.prodigal.system.exception.ThrowUtils;
import com.prodigal.system.model.dto.user.*;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.model.vo.UserVO;
import com.prodigal.system.service.UserService;
import com.prodigal.system.utils.EmailValidatorUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: system系统模块
 **/
@RestController
@RequestMapping("/sys")
public class SystemController {
    @Resource
    private UserService userService;

    @GetMapping("/hello")
    public BaseResult<String> hello() {
        return BaseResult.success().data("hello! Prodigal Picture");
    }

    /**
     * 用户注册
     *
     * @param registerDto 注册参数
     * @return 注册结果
     */
    @PostMapping("/register")
    public BaseResult<String> register(@RequestBody RegisterDto registerDto) {
        ThrowUtils.throwIf(registerDto == null, ErrorCode.PARAMS_ERROR);
        long register = userService.register(registerDto);
        return ResultUtils.success(String.valueOf(register));
    }

    @PostMapping("/login")
    public BaseResult<UserVO> login(@RequestBody LoginDto loginDto, HttpServletRequest request) {
        ThrowUtils.throwIf(loginDto == null, ErrorCode.PARAMS_ERROR);
        UserVO userVO = userService.login(loginDto, request);
        return ResultUtils.success(userVO);
    }

    @PostMapping("/logout")
    public BaseResult<Boolean> logout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(userService.logout(request));
    }

    @GetMapping("/getLoginUser")
    public BaseResult<UserVO> getLoginUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.getUserVO(loginUser));
    }

    @PostMapping("/addUser")
    @PermissionCheck(mustRole ={UserConstant.ADMIN_ROLE,UserConstant.SUPER_ADMIN_ROLE})
    public BaseResult<Long> addUser(@RequestBody UserAddDto userAddDto) {
        ThrowUtils.throwIf(userAddDto == null, ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtils.copyProperties(userAddDto, user);
        String userEmail = userAddDto.getUserEmail();
        if (StrUtil.isBlank(userEmail)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱不能为空!");
        }
        if (StrUtil.isNotBlank(userEmail) && !EmailValidatorUtils.isValidEmail(userEmail)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式错误!");
        }
        //初始密码 123456
        final String DEFAULT_PASSWORD = "123456";
        String encryptPassword = userService.getEncryptPassword(DEFAULT_PASSWORD);
        user.setUserPassword(encryptPassword);
        //设置默认角色
        user.setUserRole(StringUtils.isEmpty(userAddDto.getUserRole()) ? UserConstant.DEFAULT_ROLE : userAddDto.getUserRole());
        boolean save = userService.save(user);
        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 根据ID获取用户（管理员）
     * @param id 用户ID
     * @return 用户信息（脱敏）
     */
    @GetMapping("/get")
    @PermissionCheck(mustRole ={UserConstant.SUPER_ADMIN_ROLE})
    public BaseResult<User> getUserByID(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.USER_NOT_FOUND);
        return ResultUtils.success(user);
    }

    @GetMapping("/get/vo")
    @PermissionCheck(mustRole = {UserConstant.SUPER_ADMIN_ROLE,UserConstant.ADMIN_ROLE})
    public BaseResult<UserVO> getUserVOByID(long id) {
        BaseResult<User> res = getUserByID(id);
        User user = res.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/delete")
    @PermissionCheck(mustRole = UserConstant.SUPER_ADMIN_ROLE)
    public BaseResult<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        boolean removeById = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(removeById);
    }

    /**
     * 更新用户信息(管理员和本人)
     */
    @PostMapping("/update")
    @PermissionCheck(mustRole ={UserConstant.ADMIN_ROLE,UserConstant.SUPER_ADMIN_ROLE})
    public BaseResult<Boolean> updateUser(@RequestBody UserUpdateDto userUpdateDto){
        ThrowUtils.throwIf(userUpdateDto == null, ErrorCode.PARAMS_ERROR);
        User user = new User();
        String userEmail = userUpdateDto.getUserEmail();
        if (StrUtil.isBlank(userEmail)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱不能为空!");
        }
        if (StrUtil.isNotBlank(userEmail) && !EmailValidatorUtils.isValidEmail(userEmail)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式错误!");
        }
        BeanUtils.copyProperties(userUpdateDto, user);
        boolean update = userService.updateById(user);
        ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(update);
    }

    /**
     * 更新用户信息(管理员和本人)
     */
    @PostMapping("/edit")
    public BaseResult<Boolean> editUser(@RequestBody UserUpdateDto userUpdateDto,HttpServletRequest request){
        ThrowUtils.throwIf(userUpdateDto == null|| userUpdateDto.getId() <= 0, ErrorCode.PARAMS_ERROR);
        //应只能管理员/本人删除
        User loginUser = userService.getLoginUser(request);
        if (!loginUser.getId().equals(userUpdateDto.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.USER_NOT_PERMISSION);
        }
        String userEmail = userUpdateDto.getUserEmail();
        if (StrUtil.isBlank(userEmail)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱不能为空!");
        }
        if (StrUtil.isNotBlank(userEmail) && !EmailValidatorUtils.isValidEmail(userEmail)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式错误!");
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateDto, user);
        boolean update = userService.updateById(user);
        ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(update);
    }
    @PostMapping("/list/page/vo")
    @PermissionCheck(mustRole ={UserConstant.ADMIN_ROLE,UserConstant.SUPER_ADMIN_ROLE})
    public BaseResult<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryDto userQueryDto) {
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

