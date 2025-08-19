package com.prodigal.system.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prodigal.system.annotation.PermissionCheck;
import com.prodigal.system.common.BaseResult;
import com.prodigal.system.common.ResultUtils;
import com.prodigal.system.constant.UserConstant;
import com.prodigal.system.exception.BusinessException;
import com.prodigal.system.exception.ErrorCode;
import com.prodigal.system.exception.ThrowUtils;
import com.prodigal.system.model.dto.email.EmailQueryDto;
import com.prodigal.system.model.dto.email.EmailDto;
import com.prodigal.system.model.dto.email.EmailRequest;
import com.prodigal.system.model.entity.Email;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.model.vo.EmailVO;
import com.prodigal.system.model.vo.UserVO;
import com.prodigal.system.service.EmailService;
import com.prodigal.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 邮件control 层
 **/
@RestController
@RequestMapping("/email")
public class EmailController {
    @Lazy
    @Resource
    private UserService userService;
    @Resource
    private EmailService emailService;
    @Resource
    private MongoTemplate mongoTemplate;
    @Autowired
    private StringRedisTemplate redisTemplate;
    /**
     * 发送验证码
     *
     * @param request
     * @return
     */
    @PostMapping("/send/captcha")
    public BaseResult<String> sendVerificationCode(@Valid @RequestBody EmailRequest request) {
//        String captcha = redisTemplate.opsForValue().get("verification:code:" + request.getEmail());
//        if (StrUtil.isNotBlank(captcha)) {
//            return BaseResult.error().msg("验证码已发送，请勿重复发送");
//        }
        String verificationCode = emailService.generateVerificationCode();
        emailService.sendVerificationEmail(request.getEmail(), verificationCode);
        // 实际项目中应该将验证码存储到缓存或数据库，并设置过期时间
        // 这里只是示例，实际应用中需要处理验证码的存储和验证
        return BaseResult.success().msg("验证码已发送");
    }
    /**
     * 新增邮件草稿
     *
     * @param emailDto
     * @param request
     * @return
     */
    @PostMapping("/add")
    @PermissionCheck(mustRole = {UserConstant.ADMIN_ROLE, UserConstant.SUPER_ADMIN_ROLE})
    public BaseResult<String> addEmail(@RequestBody EmailDto emailDto, HttpServletRequest request) {
        ThrowUtils.throwIf(emailDto == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        String messageId = emailService.addEmail(emailDto, loginUser,true);
        return ResultUtils.success(messageId);
    }

    /**
     * 编辑邮件信息
     *
     * @param emailDto
     * @param request
     * @return
     */
    @PostMapping("/update")
    @PermissionCheck(mustRole = {UserConstant.ADMIN_ROLE, UserConstant.SUPER_ADMIN_ROLE})
    public BaseResult<Boolean> updateEmail(@RequestBody EmailDto emailDto, HttpServletRequest request) {
        ThrowUtils.throwIf(emailDto == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        emailService.updateEmail(emailDto, loginUser);

        return ResultUtils.success(true);
    }

    @PostMapping("/delete/{emailId}")
    public BaseResult<Boolean> deleteEmail(@PathVariable String emailId, HttpServletRequest request) {
        ThrowUtils.throwIf(emailId == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        emailService.deleteEmail(emailId, loginUser);
        return ResultUtils.success(true);
    }




    /**
     * 读取邮件（管理员或者邮件接收人是当前登录用户）
     */
    @GetMapping("/get/{emailId}")
    public BaseResult<EmailVO> getEmailById(@PathVariable String emailId, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Email email = mongoTemplate.findById(emailId, Email.class);
        ThrowUtils.throwIf(email == null, ErrorCode.NOT_FOUND_ERROR, "Email not found for ID: " + emailId);
        if (!userService.isAdmin(loginUser) && !email.getTo().equals(loginUser.getUserEmail())) {
            throw new BusinessException(ErrorCode.USER_NOT_PERMISSION, "没有权限访问");
        }
        return ResultUtils.success(EmailVO.objToVO(email));
    }

    /**
     * 读取邮件（读取当前登录用户所接受的所有邮件，也可根据条件查询）
     */
    @PostMapping("/list")
    public BaseResult<Page<EmailVO>> listEmailByPage(@RequestBody EmailQueryDto queryEmailDto, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Page<Email> emailPage = emailService.listEmail(queryEmailDto, loginUser);
        Page<EmailVO> emailVOPage = emailService.getEmailVOPage(emailPage, request);
        return ResultUtils.success(emailVOPage);
    }



    /**
     * 发送邮件（管理员-发送公告、告警）
     *
     * @param sendEmailDto
     * @param request
     * @return
     */
    @PostMapping("/send")
    @PermissionCheck(mustRole = {UserConstant.ADMIN_ROLE, UserConstant.SUPER_ADMIN_ROLE})
    public BaseResult<Boolean> sendEmail(@RequestBody EmailDto sendEmailDto, HttpServletRequest request) {
        ThrowUtils.throwIf(sendEmailDto == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
//        emailService.sendSimpleMessage(emailSendDto, loginUser);
        emailService.sendEmailByMimeMessage(sendEmailDto, loginUser);

        return ResultUtils.success(true);
    }

    @PostMapping("/send/{emailId}")
    @PermissionCheck(mustRole = {UserConstant.ADMIN_ROLE, UserConstant.SUPER_ADMIN_ROLE})
    public BaseResult<Boolean> sendEmailById(@PathVariable String emailId, HttpServletRequest request) {
        ThrowUtils.throwIf(emailId == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        emailService.sendMessageById(emailId, loginUser);

        return ResultUtils.success(true);
    }

}
