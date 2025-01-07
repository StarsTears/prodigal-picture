package com.prodigal.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mongodb.client.result.DeleteResult;
import com.prodigal.system.config.MailConfig;
import com.prodigal.system.exception.BusinessException;
import com.prodigal.system.exception.ErrorCode;
import com.prodigal.system.exception.ThrowUtils;
import com.prodigal.system.model.dto.email.EmailQueryDto;
import com.prodigal.system.model.dto.email.EmailDto;
import com.prodigal.system.model.entity.Email;
import com.prodigal.system.model.entity.Picture;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.model.enums.EmailTypeEnum;
import com.prodigal.system.model.vo.EmailVO;
import com.prodigal.system.model.vo.UserVO;
import com.prodigal.system.service.EmailService;
import com.prodigal.system.service.UserService;
import com.prodigal.system.utils.EmailValidatorUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 邮件服务类
 **/
@Slf4j
@Service
public class EmailServiceImpl implements EmailService {
//    @Value("${spring.mail.username}")
//    private String fromEmail;

    @Resource
    private MailConfig mailConfig;
    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private UserService userService;
    private final JavaMailSender emailSender;
    private static final String personal = "Prodigal Pictutre-云图库";

    @Autowired
    public EmailServiceImpl(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }
    
    @Override
    public String addEmail(EmailDto emailDto, User loginUser,boolean isAdd) {
        ThrowUtils.throwIf(emailDto == null, ErrorCode.PARAMS_ERROR);
        //校验邮箱格式、邮件类型
        this.validEmail(emailDto);
 
        //根据邮箱查询用户
        //todo 目前向一个用户发送邮件和全部用户发送邮件，（向部分用户）暂时不处理
        if (ObjUtil.isNotNull(emailDto.getReceiveUserId())) {
//            List<User> users = userService.listByIds(emailDto.getReceiveUserId());
            Long receiveUseId = emailDto.getReceiveUserId();
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.select("id","userEmail");
            User user = userService.getOne(wrapper);
            if (ObjectUtil.isNull(user)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
            }
            String userEmail = user.getUserEmail();
            if (StrUtil.isBlank(userEmail)){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户邮箱未填写");
            }
            emailDto.setTo(userEmail);
        }
//        if (CollUtil.isNotEmpty(emailDto.getTo())){
//            QueryWrapper<User> wrapper = new QueryWrapper<>();
//            wrapper.select("id","userEmail");
//            wrapper.in("userEmail", emailDto.getTo());
//            userService.getBaseMapper().selectObjs(wrapper)
//                    .stream().forEach(obj -> {
//                        
//                    });
//        }
        
        
        Email message = new Email();
        this.fillEmailParams(message, emailDto);
        if (isAdd) {
            message.setCreateTime(new Date());
            message.setCreateUserId(loginUser.getId());
        }else{
            message.setUpdateTime(new Date());
            message.setUpdateUserId(loginUser.getId());
        }
        message = mongoTemplate.save(message);
        return message.getId();
    }

    /**
     * 发送邮件
     *
     * @param emailDto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendEmailBySimpleMessage(EmailDto emailDto, User loginUser) {
        this.validEmail(emailDto);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailConfig.getUsername());
        message.setTo(JSONUtil.toJsonStr(emailDto.getTo()));
        message.setSubject(emailDto.getSubject());
        message.setText(emailDto.getTxt());
        emailSender.send(message);

        Email email = new Email();
        this.fillEmailParams(email, emailDto);
        email.setSendTime(new Date());
        email.setStatus(2);
        mongoTemplate.save(email);
    }

    /**
     * 使用MimeMessageHelper 发送邮件（可发送HTML、附件）
     *
     * @param emailDto
     * @param loginUser
     */
    @Override
    public void sendEmailByMimeMessage(EmailDto emailDto, User loginUser) {
       this.validEmail(emailDto);
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        try {
            EmailTypeEnum emailTypeEnum = EmailTypeEnum.getEnumByValue(emailDto.getType());
            helper.setFrom(mailConfig.getUsername(), personal);
            if (EmailTypeEnum.NOTICE.equals(emailTypeEnum)){
                List<String> emailList = userService.getBaseMapper()
                                                    .selectObjs(
                                                            new QueryWrapper<User>()
                                                            .select("userEmail")
                                                            .eq("isDelete", 0))
                                                    .stream()
                                                    .map(obj -> (String) obj).collect(Collectors.toList());
                helper.setTo(emailList.toArray(new String[0]));
            }else{
                helper.setTo(emailDto.getTo());
            }
            helper.setSubject(emailDto.getSubject());
            //邮件正文
            helper.setText(emailDto.getTxt(), emailDto.isHtml()); // 第二个参数 true 表示 HTML 邮件
            //有附件
            if (emailDto.getAttachments() != null && !emailDto.getAttachments().isEmpty()) {
                for (String attachment : emailDto.getAttachments()) {
                    helper.addAttachment(attachment, new File(attachment));
                }
            }
            emailSender.send(message);

            // 保存邮件信息到 MongoDB
            Email email = new Email();
            this.fillEmailParams(email, emailDto);
            email.setSendTime(new Date());
            email.setStatus(2);
            mongoTemplate.save(email);
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
        Email email = mongoTemplate.findById(emailId, Email.class);
        ThrowUtils.throwIf(email == null, ErrorCode.NOT_FOUND_ERROR, "Email not found for ID: " + emailId);
        if (StrUtil.isBlank(email.getTo())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮件收件人不能为空");
        }
        // 如果 message 存在，执行发送邮件的逻辑
        EmailDto sendEmailDto = new EmailDto();
        BeanUtils.copyProperties(email, sendEmailDto);
        sendEmailDto.setAttachments(JSONUtil.toList(email.getAttachments(), String.class));
        this.sendEmailByMimeMessage(sendEmailDto, loginUser);
    }

    Query fillParamsByCriteria(EmailQueryDto queryEmailDto) {
        Query query = new Query();
//        Criteria criteria = new Criteria();
        if (StrUtil.isNotBlank(queryEmailDto.getTo())) {
//            criteria.andOperator(Criteria.where("to").is(queryEmailDto.getTo()));
            query.addCriteria(Criteria.where("to").is(queryEmailDto.getTo()));
        }
        //邮件状态
        if (ObjectUtil.isNotEmpty(queryEmailDto.getStatus())) {
//            criteria.andOperator(Criteria.where("status").is(queryEmailDto.getStatus()));
            query.addCriteria(Criteria.where("status").is(queryEmailDto.getStatus()));
        }
        //邮件类型
        if (ObjectUtil.isNotEmpty(queryEmailDto.getType())) {
//            criteria.andOperator(Criteria.where("status").is(queryEmailDto.getStatus()));
            query.addCriteria(Criteria.where("type").is(queryEmailDto.getType()));
        }
        if (StrUtil.isNotBlank(queryEmailDto.getSubject())) {
            Pattern pattern= Pattern.compile("^.*"+queryEmailDto.getSubject()+".*$", Pattern.CASE_INSENSITIVE);
//            criteria.andOperator(Criteria.where("subject").regex(pattern));
            query.addCriteria(Criteria.where("subject").regex(pattern));
        }
        //邮件内容 应用正则
        if (StrUtil.isNotBlank(queryEmailDto.getTxt())) {
            Pattern pattern= Pattern.compile("^.*"+queryEmailDto.getTxt()+".*$", Pattern.CASE_INSENSITIVE);
//            criteria.andOperator(Criteria.where("txt").regex(pattern));
            query.addCriteria(Criteria.where("txt").regex(pattern));
        }
        
        return query;
    }


    @Override
    public Page<Email> listEmail(EmailQueryDto queryEmailDto, User loginUser) {
        //获取分页大小，及当前页
        int current = (int) queryEmailDto.getCurrent();
        int pageSize = (int) queryEmailDto.getPageSize();
        Pageable pageable = PageRequest.of(current,pageSize, Sort.by("createTime").descending());
        Query query = fillParamsByCriteria(queryEmailDto);
        // 查询总条数
        long total = mongoTemplate.count(query, Email.class, "email");

        query.with(pageable);
        //获取总页数
        long pages = (long)Math.ceil(total/pageSize) ;
        //mongo分页通过skip和limit
        long skip = (long) (current - 1) *pageSize;
        query.skip(skip).limit(pageSize);
        List<Email> emailList = mongoTemplate.find(query, Email.class);

        Page<Email> emailPage = new Page<>(current, pageSize, total,false);
        return emailPage.setRecords(emailList);
    }

    /**
     * 分页图片查询(VO封装)
     *
     * @param emailPage 分页数据
     * @param request     浏览器请求
     */
    @Override
    public Page<EmailVO> getEmailVOPage(Page<Email> emailPage, HttpServletRequest request) {
        List<Email> emailList = emailPage.getRecords();
        Page<EmailVO> emailVOPage = new Page<>(emailPage.getCurrent(), emailPage.getSize(), emailPage.getTotal());
        if (CollUtil.isEmpty(emailList)) {
            return emailVOPage;
        }
        //转换VO
        List<EmailVO> emailVOList = emailList.stream().map(EmailVO::objToVO).collect(Collectors.toList());
        //获取UserVO
        Set<Long> userIDSet = emailList.stream().map(Email::getReceiveUserId).collect(Collectors.toSet());

        Map<Long, List<User>> userIDUserListMap = userService.listByIds(userIDSet).stream().collect(Collectors.groupingBy(User::getId));
        emailVOList.forEach(e -> {
            Long receiveUserId = e.getReceiveUserId();
            if (userIDUserListMap.containsKey(receiveUserId))
                e.setReceiveUserVO(userService.getUserVO(userIDUserListMap.get(receiveUserId).get(0)));
        });
        emailVOPage.setRecords(emailVOList);
        return emailVOPage;
    }

    /**
     * 编辑邮件
     *
     * @param emailDto
     * @param loginUser
     */
    @Override
    public void updateEmail(EmailDto emailDto, User loginUser) {
        ThrowUtils.throwIf(emailDto==null || emailDto.getId() == null, ErrorCode.PARAMS_ERROR);
        Email oldEmail = mongoTemplate.findById(emailDto.getId(), Email.class);
        ThrowUtils.throwIf(oldEmail == null, ErrorCode.NOT_FOUND_ERROR, "Email not found for ID: " + emailDto.getId());

        if (oldEmail.getStatus()==2){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "邮件已发送，无法修改!!!");
        }

        this.validEmail(emailDto);

        Email message = new Email();
        this.fillEmailParams(message, emailDto);
        message.setUpdateTime(new Date());
        message.setUpdateUserId(loginUser.getId());
        mongoTemplate.save(message);
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
        ThrowUtils.throwIf(emailId == null, ErrorCode.PARAMS_ERROR);
        Query query = new Query(Criteria.where("id").is(emailId));
        DeleteResult remove = mongoTemplate.remove(query, Email.class);
        log.info("删除邮件成功，邮件id:{},受影响行数：{}", emailId,remove.getDeletedCount());
    }

    /**
     * 保存邮件消息
     *
     * @param email
     * @param emailDto
     * @return
     */
    @Override
    public void fillEmailParams(Email email, EmailDto emailDto) {
        BeanUtils.copyProperties(emailDto, email);
        email.setStatus(emailDto.getStatus());
        email.setCreateTime(new Date());
        if (emailDto.getAttachments() != null) {
            email.setAttachments(JSONUtil.toJsonStr(emailDto.getAttachments()));
        }
        if (StrUtil.isNotBlank(emailDto.getTo())) {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                    .in(User::getUserEmail, emailDto.getTo())
                    .eq(User::getIsDelete, 0);
            User user = userService.getBaseMapper().selectOne(wrapper);
            ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "User not found for email: " + emailDto.getTo());
            email.setReceiveUserId(user.getId());
        }else{
            email.setReceiveUserId(null);
        }

        //存在email_id 就将 创建人及创建时间 记录
        if (StrUtil.isNotBlank(emailDto.getId())) {
            Email oldEmail = mongoTemplate.findById(emailDto.getId(), Email.class);
            if (ObjectUtil.isNotEmpty(oldEmail)) {
                // 如果 message 存在，执行发送邮件的逻辑
                email.setCreateTime(oldEmail.getCreateTime());
                email.setCreateUserId(oldEmail.getCreateUserId());
                email.setSendTime(oldEmail.getSendTime());
                if (oldEmail.getStatus() != 0) {
                    email.setStatus(oldEmail.getStatus());
                }
            }
        }
    }

    /**
     * 邮箱格式、类型 校验
     * @param emailDto
     */
    private void validEmail(EmailDto emailDto){
        String to = emailDto.getTo();
        if (StrUtil.isNotBlank(to)){
            if (!EmailValidatorUtils.isValidEmail(to)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "以下邮箱格式错误:" + to);
            }
        }
        Integer type = emailDto.getType();
        EmailTypeEnum enumByValue = EmailTypeEnum.getEnumByValue(type);
        if (type!=null && enumByValue == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮件类型错误");
        }
    }

}