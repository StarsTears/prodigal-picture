package com.prodigal.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prodigal.system.annotation.PermissionCheck;
import com.prodigal.system.annotation.RateLimit;
import com.prodigal.system.common.BaseResult;
import com.prodigal.system.common.PageRequest;
import com.prodigal.system.common.ResultUtils;
import com.prodigal.system.constant.UserConstant;
import com.prodigal.system.exception.BusinessException;
import com.prodigal.system.exception.ErrorCode;
import com.prodigal.system.exception.ThrowUtils;
import com.prodigal.system.model.dto.email.EmailAddDTO;
import com.prodigal.system.model.dto.email.EmailQueryDTO;
import com.prodigal.system.model.dto.email.EmailRequest;
import com.prodigal.system.model.dto.email.EmailSendDTO;
import com.prodigal.system.model.dto.email.EmailUpdateDTO;
import com.prodigal.system.model.entity.Email;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.model.vo.EmailVO;
import com.prodigal.system.service.EmailService;
import com.prodigal.system.service.UserService;
import com.prodigal.system.model.enums.EmailStatusEnum;
import com.prodigal.system.model.enums.EmailTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 邮件control 层
 **/
@Slf4j
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

    /**
     * 发送登录验证码：验证码写入 Redis，邮件经 RabbitMQ 异步发送
     */
    @RateLimit(maxRequests = 3, window = 60)
    @PostMapping("/send/captcha")
    public BaseResult sendVerificationCode(@Valid @RequestBody EmailRequest request) {
        emailService.sendVerificationCodeAsync(request.getEmail());
        return BaseResult.success().msg("验证码已发送，请查收邮箱!");
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
    public BaseResult<String> addEmail(@Valid @RequestBody EmailAddDTO emailDto, HttpServletRequest request) {
        ThrowUtils.throwIf(emailDto == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        String messageId = emailService.addEmail(emailDto, loginUser);
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
    public BaseResult<Boolean> updateEmail(@Valid @RequestBody EmailUpdateDTO emailDto, HttpServletRequest request) {
        ThrowUtils.throwIf(emailDto == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        emailService.updateEmail(emailDto, loginUser);
        return ResultUtils.success(true);
    }

    @PostMapping("/delete/{emailId}")
    public BaseResult<Boolean> deleteEmail(@PathVariable("emailId") String emailId, HttpServletRequest request) {
        ThrowUtils.throwIf(emailId == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        emailService.deleteEmail(emailId, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 读取邮件（管理员或者邮件接收人是当前登录用户）
     */
    @GetMapping("/get/{emailId}")
    public BaseResult<EmailVO> getEmailById(@PathVariable("emailId") String emailId, HttpServletRequest request) {
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
    @PostMapping("/page")
    public BaseResult<Page<EmailVO>> listEmailByPage(@RequestBody EmailQueryDTO queryEmailDTO, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Page<Email> emailPage = emailService.listEmail(queryEmailDTO, loginUser);
        Page<EmailVO> emailVOPage = emailService.getEmailVOPage(emailPage, request);
        return ResultUtils.success(emailVOPage);
    }

    /**
     * 公告列表（无需登录，仅分页，固定查询 type=公告 + status=已发）
     */
    @PostMapping("/notice/page")
    public BaseResult<Page<EmailVO>> listNoticeByPage(@RequestBody PageRequest pageRequest) {
        EmailQueryDTO queryEmailDTO = new EmailQueryDTO();
        queryEmailDTO.setCurrent(pageRequest.getCurrent());
        queryEmailDTO.setPageSize(pageRequest.getPageSize());
        queryEmailDTO.setType(EmailTypeEnum.NOTICE.getValue());
        queryEmailDTO.setStatus(EmailStatusEnum.SENT.getValue());
        Page<Email> emailPage = emailService.listEmail(queryEmailDTO, null);
        Page<EmailVO> emailVOPage = emailService.getEmailVOPage(emailPage, null);
        return ResultUtils.success(emailVOPage);
    }

    /**
     * 发送邮件（管理员-发送公告、告警）
     *
     * @param sendEmailDTO
     * @param request
     * @return
     */
    @PostMapping("/send")
    @PermissionCheck(mustRole = {UserConstant.ADMIN_ROLE, UserConstant.SUPER_ADMIN_ROLE})
    public BaseResult<Boolean> sendEmail(@Valid @RequestBody EmailSendDTO sendEmailDTO, HttpServletRequest request) {
        ThrowUtils.throwIf(sendEmailDTO == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        emailService.sendEmailByMimeMessage(sendEmailDTO, loginUser);
        return ResultUtils.success(true);
    }

    @PostMapping("/send/{emailId}")
    @PermissionCheck(mustRole = {UserConstant.ADMIN_ROLE, UserConstant.SUPER_ADMIN_ROLE})
    public BaseResult<Boolean> sendEmailById(@PathVariable("emailId") String emailId, HttpServletRequest request) {
        ThrowUtils.throwIf(emailId == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        emailService.sendMessageById(emailId, loginUser);

        return ResultUtils.success(true);
    }

}
