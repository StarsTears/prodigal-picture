package com.prodigal.system.controller;

import com.prodigal.system.annotation.RateLimit;
import com.prodigal.system.common.BaseResult;
import com.prodigal.system.common.ResultUtils;
import com.prodigal.system.constant.LoginConstant;
import com.prodigal.system.constant.UserConstant;
import com.prodigal.system.exception.BusinessException;
import com.prodigal.system.exception.ErrorCode;
import com.prodigal.system.exception.ThrowUtils;
import com.prodigal.system.manager.auth.StpKit;
import com.prodigal.system.model.dto.system.LoginDTO;
import com.prodigal.system.model.dto.system.RegisterDTO;
import com.prodigal.system.model.dto.system.ResetPasswordDTO;
import com.prodigal.system.model.dto.user.UserAddDTO;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.model.vo.UserVO;
import com.prodigal.system.service.EmailService;
import com.prodigal.system.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.bind.annotation.*;

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
            StpKit.SPACE.login(user.getId());
            StpKit.SPACE.getSession().set(UserConstant.USER_LOGIN_STATE, user);
            return ResultUtils.success(userService.getUserVO(user));
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的登录类型");
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

    @PostMapping("/logout")
    public BaseResult<Boolean> logout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(userService.logout(request));
    }
}

