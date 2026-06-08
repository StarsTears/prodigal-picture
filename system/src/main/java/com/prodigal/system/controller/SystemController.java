package com.prodigal.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prodigal.system.annotation.PermissionCheck;
import com.prodigal.system.annotation.RateLimit;
import com.prodigal.system.common.BaseResult;
import com.prodigal.system.common.DeleteRequest;
import com.prodigal.system.common.ResultUtils;
import com.prodigal.system.constant.LoginConstant;
import com.prodigal.system.constant.UserConstant;
import com.prodigal.system.exception.BusinessException;
import com.prodigal.system.exception.ErrorCode;
import com.prodigal.system.exception.ThrowUtils;
import com.prodigal.system.model.dto.user.*;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.model.vo.UserVO;
import com.prodigal.system.service.EmailService;
import com.prodigal.system.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

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
    @Resource
    private EmailService emailService;

    @GetMapping("/hello")
    public BaseResult<String> hello() {
        return BaseResult.<String>success().data("hello! Prodigal Picture");
    }

    /**
     * 用户注册
     *
     * @param registerDto 注册参数
     * @return 注册结果
     */
    @RateLimit(maxRequests = 5, window = 60)
    @PostMapping("/register")
    public BaseResult<String> register(@Valid @RequestBody RegisterDTO registerDto) {
        ThrowUtils.throwIf(registerDto == null, ErrorCode.PARAMS_ERROR);
        long register = userService.register(registerDto);
        return ResultUtils.success(String.valueOf(register));
    }

    @RateLimit(maxRequests = 10, window = 60)
    @PostMapping("/login")
    public BaseResult<UserVO> login(@Valid @RequestBody LoginDTO loginDto, HttpServletRequest request) {
        ThrowUtils.throwIf(loginDto == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginDto.getLoginType() == null, ErrorCode.PARAMS_ERROR);
        if (loginDto.getLoginType().equals(LoginConstant.USER_LOGIN_TYPE)) {
            UserVO userVO = userService.login(loginDto, request);
            return ResultUtils.success(userVO);
        }
        if (loginDto.getLoginType().equals(LoginConstant.EMAIL_LOGIN_TYPE)) {
            // 校验验证码
            boolean valid = emailService.verifyCode(loginDto.getEmail(), loginDto.getCaptcha());
            ThrowUtils.throwIf(!valid, ErrorCode.CAPTCHA_ERROR);
            // 校验用户是否存在
            User user = userService.lambdaQuery().eq(User::getUserEmail, loginDto.getEmail()).one();
            //用户不存在就创建一个；
            if (ObjectUtils.isEmpty(user)){
                UserAddDTO userAddDto = new UserAddDTO();
                userAddDto.setUserEmail(loginDto.getEmail());
                userAddDto.setUserAccount(loginDto.getEmail());
                userAddDto.setUserName(loginDto.getEmail());

                userService.createUser(userAddDto);
                user = userService.lambdaQuery().eq(User::getUserEmail, loginDto.getEmail()).one();
            }
            // 写入登录态
            request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
            // Sa-token登录
            com.prodigal.system.manager.auth.StpKit.SPACE.login(user.getId());
            com.prodigal.system.manager.auth.StpKit.SPACE.getSession().set(UserConstant.USER_LOGIN_STATE, user);
            return ResultUtils.success(userService.getUserVO(user));
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的登录类型");
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
    @PermissionCheck(mustRole = {UserConstant.ADMIN_ROLE, UserConstant.SUPER_ADMIN_ROLE})
    public BaseResult<Long> addUser(@Valid @RequestBody UserAddDTO userAddDto) {
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
    public BaseResult<User> getUserByID(@RequestParam("id") long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.USER_NOT_FOUND);
        return ResultUtils.success(user);
    }

    @GetMapping("/get/vo")
    @PermissionCheck(mustRole = {UserConstant.SUPER_ADMIN_ROLE, UserConstant.ADMIN_ROLE})
    public BaseResult<UserVO> getUserVOByID(@RequestParam("id") long id) {
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
        boolean removeById = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(removeById);
    }

    /**
     * 更新用户信息(管理员和本人)
     */
    @PostMapping("/update")
    @PermissionCheck(mustRole = {UserConstant.ADMIN_ROLE, UserConstant.SUPER_ADMIN_ROLE})
    public BaseResult<Boolean> updateUser(@Valid @RequestBody UserUpdateDTO userUpdateDto) {
        ThrowUtils.throwIf(userUpdateDto == null, ErrorCode.PARAMS_ERROR);
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
        //应只能管理员/本人删除
        User loginUser = userService.getLoginUser(request);
        if (!loginUser.getId().equals(userUpdateDto.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.USER_NOT_PERMISSION);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateDto, user);
        boolean update = userService.updateById(user);
        ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(update);
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

    /**
     * 忘记密码 - 重置密码
     */
    @RateLimit(maxRequests = 3, window = 60)
    @PostMapping("/reset-password")
    public BaseResult<Boolean> resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
        ThrowUtils.throwIf(dto == null, ErrorCode.PARAMS_ERROR);
        boolean valid = emailService.verifyCode(dto.getUserEmail(), dto.getCaptcha());
        ThrowUtils.throwIf(!valid, ErrorCode.CAPTCHA_ERROR);
        userService.resetPassword(dto);
        return ResultUtils.success(true);
    }
}

