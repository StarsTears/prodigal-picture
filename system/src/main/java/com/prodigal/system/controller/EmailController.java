package com.prodigal.system.controller;

import cn.hutool.core.util.StrUtil;
import com.prodigal.system.annotation.PermissionCheck;
import com.prodigal.system.common.BaseResult;
import com.prodigal.system.common.ResultUtils;
import com.prodigal.system.constant.UserConstant;
import com.prodigal.system.exception.BusinessException;
import com.prodigal.system.exception.ErrorCode;
import com.prodigal.system.exception.ThrowUtils;
import com.prodigal.system.mapper.EmailMapper;
import com.prodigal.system.model.dto.email.QueryEmailDto;
import com.prodigal.system.model.dto.email.SendEmailDto;
import com.prodigal.system.model.entity.Email;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.service.EmailService;
import com.prodigal.system.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 邮件control 层
 **/
@RestController
@RequestMapping("/email")
public class EmailController {
    @Resource
    private UserService userService;
    @Resource
    private EmailService emailService;
    @Resource
    private EmailMapper emailMapper;

    /**
     * 读取邮件（管理员或者邮件接收人是当前登录用户）
     */
    @GetMapping("/get/{emailId}")
    public BaseResult<Email> getEmailById(@PathVariable String emailId, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Optional<Email> emailOptional = emailMapper.findById(emailId);
        Email email = emailOptional.orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "Email not found for ID: " + emailId));
        if (!userService.isAdmin(loginUser) && !email.getTo().equals(loginUser.getUserEmail())) {
            throw new BusinessException(ErrorCode.USER_NOT_PERMISSION, "没有权限访问");
        }
        return ResultUtils.success(email);
    }

    /**
     * 读取邮件（读取当前登录用户所接受的所有邮件，也可根据条件查询）
     */
    @PostMapping("/list")
    public BaseResult<List<Email>> listEmail(@RequestBody Optional<QueryEmailDto> queryEmailDtoOptional, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        List<Email> emailList = new ArrayList<>();

        if (userService.isAdmin(loginUser)) {
            if (queryEmailDtoOptional.isPresent()) {
                QueryEmailDto queryEmailDto = queryEmailDtoOptional.get();
                String emailId = queryEmailDto.getId();
                String to = queryEmailDto.getTo();
                String subject = queryEmailDto.getSubject();
                String txt = queryEmailDto.getTxt();
                if (StrUtil.isNotBlank(emailId)) {
                    emailList = emailMapper.findById(emailId).map(Collections::singletonList).orElse(Collections.emptyList());
                } else if (StrUtil.isNotBlank(to)  && StrUtil.isNotBlank(subject)  && StrUtil.isNotBlank(txt)) {
                    emailList=emailMapper.findByToAndSubjectContainsIgnoreCaseAndTxtContainsIgnoreCase(to, subject, txt);
                } else if (StrUtil.isNotBlank(to) && StrUtil.isNotBlank(subject)) {
                    emailList = emailMapper.findByToAndSubjectContainsIgnoreCase(to, subject);
                } else if (StrUtil.isNotBlank(to) && StrUtil.isNotBlank(txt)) {
                    emailList = emailMapper.findByToAndTxtContainingIgnoreCase(to, txt);
                } else if (StrUtil.isNotBlank(subject) && StrUtil.isNotBlank(txt)) {
                    emailList = emailMapper.findBySubjectContainsIgnoreCaseAndTxtContainsIgnoreCase(subject, txt);
                } else if (StrUtil.isNotBlank(subject)) {
                    emailList = emailMapper.findBySubjectContainsIgnoreCase(subject);
                } else if (StrUtil.isNotBlank(txt)) {
                    emailList = emailMapper.findByTxtContainsIgnoreCase(txt);
                } else if (StrUtil.isNotBlank(to)) {
                    emailList = emailMapper.findByTo(to);
                }else{
                    emailList = emailMapper.findAll();
                }
            } else {
                emailList = emailMapper.findAll();
            }
        }
        return ResultUtils.success(emailList);
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
    public BaseResult<Boolean> sendEmail(@RequestBody SendEmailDto sendEmailDto, HttpServletRequest request) {
        ThrowUtils.throwIf(sendEmailDto == null || StrUtil.isBlank(sendEmailDto.getTo()), ErrorCode.PARAMS_ERROR);
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

    /**
     * 新增邮件草稿
     *
     * @param sendEmailDto
     * @param request
     * @return
     */
    @PostMapping("/add")
    @PermissionCheck(mustRole = {UserConstant.ADMIN_ROLE, UserConstant.SUPER_ADMIN_ROLE})
    public BaseResult<String> addEmail(@RequestBody SendEmailDto sendEmailDto, HttpServletRequest request) {
        ThrowUtils.throwIf(sendEmailDto == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        String messageId = emailService.addEmail(sendEmailDto, loginUser);
        return ResultUtils.success(messageId);
    }

    /**
     * 编辑邮件信息
     *
     * @param sendEmailDto
     * @param request
     * @return
     */
    @PostMapping("/edit")
    @PermissionCheck(mustRole = {UserConstant.ADMIN_ROLE, UserConstant.SUPER_ADMIN_ROLE})
    public BaseResult<Boolean> editEmail(@RequestBody SendEmailDto sendEmailDto, HttpServletRequest request) {
        ThrowUtils.throwIf(sendEmailDto == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        emailService.editEmail(sendEmailDto, loginUser);

        return ResultUtils.success(true);
    }

    @PostMapping("/delete/{emailId}")
    public BaseResult<Boolean> deleteEmail(@PathVariable String emailId, HttpServletRequest request) {
        ThrowUtils.throwIf(emailId == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        emailService.deleteEmail(emailId, loginUser);
        return ResultUtils.success(true);
    }
}
