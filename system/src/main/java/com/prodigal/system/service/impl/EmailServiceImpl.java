package com.prodigal.system.service.impl;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
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
import com.prodigal.system.utils.EmailValidatorUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 邮件服务类
 **/
@Slf4j
@Service
public class EmailServiceImpl implements EmailService {
    @Value("${spring.mail.username}")
    private String fromEmail;
//    @Value("${spring.mail.password}")
//    private String password;

    @Resource
    private EmailMapper emailMapper;

    @Resource
    private UserService userService;
    private final JavaMailSender emailSender;
    private static final String personal = "Prodigal Pictutre-云图库";

    @Autowired
    public EmailServiceImpl(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Override
    public String addEmail(SendEmailDto sendEmailDto, User loginUser) {
        String to = sendEmailDto.getTo();
        if (StrUtil.isNotBlank(to) && !EmailValidatorUtils.isValidEmail(sendEmailDto.getTo())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式错误");
        }
        Email message = new Email();
        this.fillEmailParams(message, sendEmailDto);
        message.setCreateTime(new Date());
        message.setCreateUserId(loginUser.getId());
        message = emailMapper.save(message);
        return message.getId();
    }

    /**
     * 发送邮件
     *
     * @param sendEmailDto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendEmailBySimpleMessage(SendEmailDto sendEmailDto, User loginUser) {
        ThrowUtils.throwIf(!EmailValidatorUtils.isValidEmail(sendEmailDto.getTo()), ErrorCode.PARAMS_ERROR, "邮箱格式错误");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(sendEmailDto.getTo());
        message.setSubject(sendEmailDto.getSubject());
        message.setText(sendEmailDto.getTxt());
        emailSender.send(message);

        Email email = new Email();
        this.fillEmailParams(email, sendEmailDto);
        email.setSendTime(new Date());
        email.setStatus(1);
        emailMapper.save(email);
    }

    /**
     * 使用MimeMessageHelper 发送邮件（可发送HTML、附件）
     *
     * @param sendEmailDto
     * @param loginUser
     */
    @Override
    public void sendEmailByMimeMessage(SendEmailDto sendEmailDto, User loginUser) {
        ThrowUtils.throwIf(!EmailValidatorUtils.isValidEmail(sendEmailDto.getTo()), ErrorCode.PARAMS_ERROR, "邮箱格式错误");
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        try {
            helper.setFrom(fromEmail, personal);
            helper.setTo(sendEmailDto.getTo());
            helper.setSubject(sendEmailDto.getSubject());
            //邮件正文
            helper.setText(sendEmailDto.getTxt(), sendEmailDto.isHtml()); // 第二个参数 true 表示 HTML 邮件
            //有附件
            if (sendEmailDto.getAttachments() != null && !sendEmailDto.getAttachments().isEmpty()) {
                for (String attachment : sendEmailDto.getAttachments()) {
                    helper.addAttachment(attachment, new File(attachment));
                }
            }
            emailSender.send(message);

            // 保存邮件信息到 MongoDB
            Email email = new Email();
            this.fillEmailParams(email, sendEmailDto);
            email.setSendTime(new Date());
            email.setStatus(1);
            emailMapper.save(email);
        } catch (MessagingException e) {
            log.error("Failed to send email:{} ", e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "Failed to send email:" + e);
        } catch (UnsupportedEncodingException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "Failed to send email setFrom:" + e);
        }
    }

    /**
     * 根据id 发送邮件
     *
     * @param emailId
     * @param loginUser
     */
    @Override
    public void sendMessageById(String emailId, User loginUser) {
        Optional<Email> messageOptional = emailMapper.findById(emailId);
        messageOptional.ifPresentOrElse(
                message -> {
                    // 如果 message 存在，执行发送邮件的逻辑
                    SendEmailDto sendEmailDto = new SendEmailDto();
                    sendEmailDto.setId(emailId);
                    sendEmailDto.setTo(message.getTo());
                    sendEmailDto.setSubject(message.getSubject());
                    sendEmailDto.setTxt(message.getTxt());
                    sendEmailDto.setHtml(message.isHtml());
                    if (message.getAttachments() != null) {
                        sendEmailDto.setAttachments(JSONUtil.toList(message.getAttachments(), String.class));
                    }
                    this.sendEmailByMimeMessage(sendEmailDto, loginUser);
                    // 你可以在这里添加更多的发送邮件逻辑
                },
                () -> {
                    // 如果 message 不存在，记录日志或抛出异常
                    log.error("Message not found for ID: {}", emailId);
                    // 你可以在这里添加更多的处理逻辑，比如抛出异常
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "Message not found for ID: " + emailId);
                }
        );
    }

    @Override
    public Email getEmailById(String emailId, User loginUser) {
        Optional<Email> emailOptional = emailMapper.findById(emailId);
        return emailOptional.orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "Email not found for ID: " + emailId));
    }

    @Override
    public List<Email> listEmail(QueryEmailDto queryEmailDto, User loginUser) {
        ThrowUtils.throwIf(queryEmailDto==null, ErrorCode.PARAMS_ERROR, "参数错误");
        List<Email> emailList = new ArrayList<>();
        if (queryEmailDto.getId() != null) {
            emailList = emailMapper.findById(queryEmailDto.getId()).map(Collections::singletonList).orElse(Collections.emptyList());
        }else if(queryEmailDto.getSubject() != null || queryEmailDto.getTxt() != null){

        }else if (queryEmailDto.getTo() != null) {
            emailList = emailMapper.findByTo(queryEmailDto.getTo());
        }
        return emailList;
    }

    /**
     * 编辑邮件
     *
     * @param sendEmailDto
     * @param loginUser
     */
    @Override
    public void editEmail(SendEmailDto sendEmailDto, User loginUser) {
        String to = sendEmailDto.getTo();
        if (StrUtil.isNotBlank(to) && !EmailValidatorUtils.isValidEmail(sendEmailDto.getTo())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式错误");
        }
        Email message = new Email();
        this.fillEmailParams(message, sendEmailDto);
        emailMapper.save(message);
    }

    /**
     * 删除邮件
     *
     * @param emailId
     * @param loginUser
     */
    @Override
    public void deleteEmail(String emailId, User loginUser) {
        //这里需校验，只有该图片的创建者或者管理员可以删除
        emailMapper.deleteById(emailId);
    }

    /**
     * 保存邮件消息
     *
     * @param email
     * @param sendEmailDto
     * @return
     */
    @Override
    public void fillEmailParams(Email email, SendEmailDto sendEmailDto) {
        BeanUtils.copyProperties(sendEmailDto, email);
        email.setSubject(sendEmailDto.getSubject());
        email.setTxt(sendEmailDto.getTxt());
        email.setTo(sendEmailDto.getTo());
        email.setHtml(sendEmailDto.isHtml());
        email.setEditTime(new Date());
        email.setStatus(0);
        email.setCreateTime(new Date());
        if (sendEmailDto.getAttachments() != null) {
            email.setAttachments(JSONUtil.toJsonStr(sendEmailDto.getAttachments()));
        }
        LambdaQueryChainWrapper<User> select = userService.lambdaQuery()
                .eq(User::getUserEmail, sendEmailDto.getTo())
                .select(User::getId);
        email.setReceiveUserId(select.oneOpt().map(User::getId).orElse(null));

        //存在email_id 就将 创建人及创建时间 记录下拉
        if (StrUtil.isNotBlank(sendEmailDto.getId())) {
            Optional<Email> messageOptional = emailMapper.findById(sendEmailDto.getId());
            messageOptional.ifPresentOrElse(
                    message -> {
                        // 如果 message 存在，执行发送邮件的逻辑
                        email.setCreateTime(message.getCreateTime());
                        email.setCreateUserId(message.getCreateUserId());
                        email.setSendTime(message.getSendTime());
                        if (message.getStatus() != 0) {
                            email.setStatus(message.getStatus());
                        }
                    },
                    () -> {
                        // 如果 message 不存在，记录日志或抛出异常
                        log.error("Message not found for ID: {}", sendEmailDto.getId());
                    }
            );
        }

    }

}
//        // 设置邮件服务器属性
//        Properties properties = new Properties();
//        properties.put("mail.smtp.host", "smtp.qq.com"); // SMTP 服务器地址
//        properties.put("mail.smtp.port", "587"); // SMTP 端口
//        properties.put("mail.smtp.auth", "true"); // 是否需要认证
//        properties.put("mail.smtp.starttls.enable", "true"); // 启用 TLS
//
//        Session session = Session.getInstance(properties, new Authenticator() {
//            @Override
//            protected PasswordAuthentication getPasswordAuthentication() {
//                return new PasswordAuthentication(fromEmail, password);
//            }
//        });
//        try {
//            // 创建邮件内容
//            Message mimeMessage = new MimeMailMessage();
//            InternetAddress from = new InternetAddress(fromEmail, "prodigal");
//            mimeMessage.setFrom(from);
//            mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailSendDto.getTo()));
//            mimeMessage.setSubject(email.getSubject());
//            mimeMessage.setText(email.getContent());
//
//            // 发送邮件
//            Transport.send(mimeMessage);
//        } catch (MessagingException e) {
//            e.printStackTrace();
//        } catch (UnsupportedEncodingException e) {
//            throw new RuntimeException(e);
//        }