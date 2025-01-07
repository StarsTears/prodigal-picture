package com.prodigal.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prodigal.system.model.dto.email.EmailQueryDto;
import com.prodigal.system.model.dto.email.EmailDto;
import com.prodigal.system.model.entity.Email;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.model.vo.EmailVO;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @create: 2024-12-27 17:43
 * @description:
 **/
public interface EmailService {
    String addEmail(EmailDto emailDto, User loginUser,boolean isAdd);
    @Transactional(rollbackFor = Exception.class)
    void sendEmailBySimpleMessage(EmailDto emailDto, User loginUser);

    void sendMessageById(String emailId, User loginUser);

    void sendEmailByMimeMessage(EmailDto emailDto, User loginUser);

    Page<EmailVO> getEmailVOPage(Page<Email> emailPage, HttpServletRequest request);

    void updateEmail(EmailDto emailDto, User loginUser);

    void deleteEmail(String emailId, User loginUser);

    void fillEmailParams(Email email, EmailDto emailDto);

    Page<Email> listEmail(EmailQueryDto queryEmailDto, User loginUser);
}
