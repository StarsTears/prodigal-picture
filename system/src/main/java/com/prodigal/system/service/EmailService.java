package com.prodigal.system.service;

import com.prodigal.system.model.dto.email.QueryEmailDto;
import com.prodigal.system.model.dto.email.SendEmailDto;
import com.prodigal.system.model.entity.Email;
import com.prodigal.system.model.entity.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @create: 2024-12-27 17:43
 * @description:
 **/
public interface EmailService {
    @Transactional(rollbackFor = Exception.class)
    void sendEmailBySimpleMessage(SendEmailDto emailSendDto, User loginUser);

    void sendMessageById(String emailId, User loginUser);

    void sendEmailByMimeMessage(SendEmailDto sendMessageDto, User loginUser);

    String addEmail(SendEmailDto emailSendMessageDto, User loginUser);

    void editEmail(SendEmailDto emailSendMessageDto, User loginUser);

    void deleteEmail(String emailId, User loginUser);

    void fillEmailParams(Email email, SendEmailDto emailSendMessageDto);

    Email getEmailById(String emailId, User loginUser);

    List<Email> listEmail(QueryEmailDto queryEmailDto, User loginUser);
}
