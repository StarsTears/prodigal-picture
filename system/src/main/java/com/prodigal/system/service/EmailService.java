package com.prodigal.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prodigal.system.model.dto.email.EmailAddDTO;
import com.prodigal.system.model.dto.email.EmailQueryDTO;
import com.prodigal.system.model.dto.email.EmailSendDTO;
import com.prodigal.system.model.dto.email.EmailUpdateDTO;
import com.prodigal.system.model.entity.Email;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.model.vo.EmailVO;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @create: 2024-12-27 17:43
 * @description:
 **/
public interface EmailService {
    /**
     * 生成验证码、写入 Redis，并通过 RabbitMQ 异步投递发送任务
     */
    void sendVerificationCodeAsync(String email);

    String addEmail(EmailAddDTO emailDto, User loginUser);

    void sendEmailByMimeMessage(EmailSendDTO emailDto, User loginUser);

    void sendMessageById(String emailId, User loginUser);

    Page<EmailVO> getEmailVOPage(Page<Email> emailPage, HttpServletRequest request);

    void updateEmail(EmailUpdateDTO emailDto, User loginUser);

    void deleteEmail(String emailId, User loginUser);

    Page<Email> listEmail(EmailQueryDTO queryEmailDTO, User loginUser);

    /**
     * 校验邮箱验证码
     */
    boolean verifyCode(String email, String code);
}
