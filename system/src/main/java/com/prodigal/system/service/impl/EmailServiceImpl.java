package com.prodigal.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mongodb.client.result.DeleteResult;
import com.prodigal.system.config.MailConfig;
import com.prodigal.system.constant.CacheConstant;
import com.prodigal.system.exception.BusinessException;
import com.prodigal.system.exception.ErrorCode;
import com.prodigal.system.exception.ThrowUtils;
import com.prodigal.system.model.dto.email.EmailAddDTO;
import com.prodigal.system.model.dto.email.EmailQueryDTO;
import com.prodigal.system.model.dto.email.EmailSendDTO;
import com.prodigal.system.model.dto.email.EmailUpdateDTO;
import com.prodigal.system.model.entity.Email;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.model.enums.EmailStatusEnum;
import com.prodigal.system.model.enums.EmailTypeEnum;
import com.prodigal.system.model.message.EmailCaptchaMessage;
import com.prodigal.system.model.message.EmailSendMessage;
import com.prodigal.system.model.vo.EmailVO;
import com.prodigal.system.mq.producer.EmailProducer;
import com.prodigal.system.service.EmailService;
import com.prodigal.system.service.UserService;
import com.prodigal.system.utils.EmailValidatorUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
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

    @Resource
    private MailConfig mailConfig;
    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private UserService userService;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Resource
    private EmailProducer emailProducer;

    @Override
    public void sendVerificationCodeAsync(String email) {
        String sendLockKey = CacheConstant.SEND_LOCK_PREFIX + email;
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(sendLockKey, "1", CacheConstant.SEND_LOCK_SECONDS, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(locked)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码已发送，请稍后再试");
        }

        String code = generateVerificationCode();
        String codeKey = CacheConstant.CODE_PREFIX + email;
        try {
            redisTemplate.opsForValue().set(
                    codeKey,
                    code,
                    CacheConstant.CODE_EXPIRE_MINUTES,
                    TimeUnit.MINUTES
            );
            emailProducer.publishCaptcha(new EmailCaptchaMessage(email, code));
        } catch (Exception e) {
            redisTemplate.delete(codeKey);
            redisTemplate.delete(sendLockKey);
            log.error("验证码投递 MQ 失败, email={}", email, e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码发送失败，请稍后重试");
        }
    }

    @Override
    public boolean verifyCode(String email, String code) {
        if (StrUtil.isBlank(email) || StrUtil.isBlank(code)) {
            return false;
        }
        String storedCode = redisTemplate.opsForValue().get(CacheConstant.CODE_PREFIX + email);
        return code.equals(storedCode);
    }



    @Override
    public String addEmail(EmailAddDTO emailDto, User loginUser) {
        ThrowUtils.throwIf(emailDto == null, ErrorCode.PARAMS_ERROR);
        validEmail(emailDto.getTo(), emailDto.getType());

        Email message = new Email();
        fillEmailFromDTO(message, emailDto.getTo(), emailDto.getType(),
                emailDto.getSubject(), emailDto.getTxt(), emailDto.isHtml(), emailDto.getAttachments());
        message.setStatus(EmailStatusEnum.DRAFT.getValue());
        message.setCreateTime(new Date());
        message.setCreateUserId(loginUser.getId());
        message = mongoTemplate.save(message);
        return message.getId();
    }

    /**
     * 使用MimeMessageHelper 发送邮件（可发送HTML、附件）
     * @param emailDto
     * @param loginUser
     */
    @Override
    public void sendEmailByMimeMessage(EmailSendDTO emailDto, User loginUser) {
        validEmail(emailDto.getTo(), emailDto.getType());

        // 告警类型必须指定收件人
        EmailTypeEnum emailTypeEnum = EmailTypeEnum.getEnumByValue(emailDto.getType());
        if (EmailTypeEnum.alert.equals(emailTypeEnum) && StrUtil.isBlank(emailDto.getTo())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "告警类型必须指定收件人");
        }

        // 公告类型：若未指定收件人，则查询全量用户邮箱
        if (EmailTypeEnum.NOTICE.equals(emailTypeEnum) && StrUtil.isBlank(emailDto.getTo())) {
            List<String> emailList = userService.getBaseMapper()
                    .selectObjs(new QueryWrapper<User>()
                            .select("userEmail")
                            .eq("isDelete", 0))
                    .stream()
                    .map(obj -> (String) obj)
                    .collect(Collectors.toList());
            emailDto.setTo(String.join(",", emailList));
        }

        // 先存 MongoDB，状态为已提交
        Email email = new Email();
        fillEmailFromDTO(email, emailDto.getTo(), emailDto.getType(),
                emailDto.getSubject(), emailDto.getTxt(), emailDto.isHtml(), emailDto.getAttachments());
        email.setStatus(EmailStatusEnum.SUBMITTED.getValue());
        email.setCreateTime(new Date());
        if (loginUser != null) {
            email.setCreateUserId(loginUser.getId());
        }
        email = mongoTemplate.save(email);

        // 投递 MQ 异步发送
        emailProducer.publishNotify(new EmailSendMessage(email.getId()));
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
        if (Integer.valueOf(EmailStatusEnum.SENT.getValue()).equals(email.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "邮件已发送，无法重复发送");
        }
        // 告警类型必须指定收件人
        EmailTypeEnum typeEnum = EmailTypeEnum.getEnumByValue(email.getType());
        if (EmailTypeEnum.alert.equals(typeEnum) && StrUtil.isBlank(email.getTo())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "告警类型必须指定收件人");
        }
        // 更新状态为已提交
        email.setStatus(EmailStatusEnum.SUBMITTED.getValue());
        mongoTemplate.save(email);

        // 投递 MQ 异步发送
        emailProducer.publishNotify(new EmailSendMessage(email.getId()));
    }

    Query fillParamsByCriteria(EmailQueryDTO queryEmailDTO) {
        Query query = new Query();
//        Criteria criteria = new Criteria();
        if (StrUtil.isNotBlank(queryEmailDTO.getTo())) {
            Pattern pattern = Pattern.compile("^.*" + queryEmailDTO.getTo() + ".*$", Pattern.CASE_INSENSITIVE);
            if (ObjectUtil.isEmpty(queryEmailDTO.getType())) {
                // 未指定邮件类型：匹配收件人或公告（公告全员可见）
                query.addCriteria(new Criteria().orOperator(
                        Criteria.where("to").regex(pattern),
                        Criteria.where("type").is(EmailTypeEnum.NOTICE.getValue())
                ));
            } else {
                query.addCriteria(Criteria.where("to").regex(pattern));
            }
        }
        //邮件状态
        if (ObjectUtil.isNotEmpty(queryEmailDTO.getStatus())) {
//            criteria.andOperator(Criteria.where("status").is(queryEmailDTO.getStatus()));
            query.addCriteria(Criteria.where("status").is(queryEmailDTO.getStatus()));
        }
        //邮件类型
        if (ObjectUtil.isNotEmpty(queryEmailDTO.getType())) {
//            criteria.andOperator(Criteria.where("status").is(queryEmailDTO.getStatus()));
            query.addCriteria(Criteria.where("type").is(queryEmailDTO.getType()));
        }
        if (StrUtil.isNotBlank(queryEmailDTO.getSubject())) {
            Pattern pattern= Pattern.compile("^.*"+queryEmailDTO.getSubject()+".*$", Pattern.CASE_INSENSITIVE);
//            criteria.andOperator(Criteria.where("subject").regex(pattern));
            query.addCriteria(Criteria.where("subject").regex(pattern));
        }
        //邮件内容 应用正则
        if (StrUtil.isNotBlank(queryEmailDTO.getTxt())) {
            Pattern pattern= Pattern.compile("^.*"+queryEmailDTO.getTxt()+".*$", Pattern.CASE_INSENSITIVE);
//            criteria.andOperator(Criteria.where("txt").regex(pattern));
            query.addCriteria(Criteria.where("txt").regex(pattern));
        }
        
        return query;
    }


    @Override
    public Page<Email> listEmail(EmailQueryDTO queryEmailDTO, User loginUser) {
        int current = (int) queryEmailDTO.getCurrent();
        int pageSize = (int) queryEmailDTO.getPageSize();
        Query query = fillParamsByCriteria(queryEmailDTO);
        long total = mongoTemplate.count(query, Email.class, "email");

        query.with(Sort.by(Sort.Direction.DESC, "createTime"));
        long skip = (long) (current - 1) * pageSize;
        query.skip(skip).limit(pageSize);
        List<Email> emailList = mongoTemplate.find(query, Email.class);

        Page<Email> emailPage = new Page<>(current, pageSize, total, false);
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
    public void updateEmail(EmailUpdateDTO emailDto, User loginUser) {
        ThrowUtils.throwIf(emailDto == null || emailDto.getId() == null, ErrorCode.PARAMS_ERROR);
        Email oldEmail = mongoTemplate.findById(emailDto.getId(), Email.class);
        ThrowUtils.throwIf(oldEmail == null, ErrorCode.NOT_FOUND_ERROR, "Email not found for ID: " + emailDto.getId());

        if (Integer.valueOf(EmailStatusEnum.SENT.getValue()).equals(oldEmail.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "邮件已发送，无法修改!!!");
        }

        validEmail(emailDto.getTo(), emailDto.getType());

        Email email = new Email();
        fillEmailFromDTO(email, emailDto.getTo(), emailDto.getType(),
                emailDto.getSubject(), emailDto.getTxt(), emailDto.isHtml(), emailDto.getAttachments());
        email.setId(emailDto.getId());
        email.setCreateTime(oldEmail.getCreateTime());
        email.setCreateUserId(oldEmail.getCreateUserId());
        email.setSendTime(oldEmail.getSendTime());
        if (emailDto.getStatus() != null) {
            email.setStatus(emailDto.getStatus());
        } else if (!Integer.valueOf(EmailStatusEnum.DRAFT.getValue()).equals(oldEmail.getStatus())) {
            email.setStatus(oldEmail.getStatus());
        }
        email.setUpdateTime(new Date());
        email.setUpdateUserId(loginUser.getId());
        mongoTemplate.save(email);
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

    private void fillEmailFromDTO(Email email, String to, Integer type, String subject,
                                   String txt, boolean isHtml, List<String> attachments) {
        email.setTo(to);
        email.setType(type);
        email.setSubject(subject);
        email.setTxt(txt);
        email.setHtml(isHtml);
        if (attachments != null) {
            email.setAttachments(JSONUtil.toJsonStr(attachments));
        }
        if (StrUtil.isNotBlank(to)) {
            String firstEmail = to.split(",")[0].trim();
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                    .eq(User::getUserEmail, firstEmail)
                    .eq(User::getIsDelete, 0);
            User user = userService.getBaseMapper().selectOne(wrapper);
            if (user != null) {
                email.setReceiveUserId(user.getId());
            }
        }
    }

    /**
     * 验证码生成
     * @return code
     */
    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    /**
     * 邮箱格式、类型 校验
     * @param to 收件人
     * @param type 邮件类型
     */
    private void validEmail(String to, Integer type) {
        if (StrUtil.isNotBlank(to)) {
            String[] emails = to.split(",");
            for (String email : emails) {
                String trimmed = email.trim();
                if (StrUtil.isNotBlank(trimmed) && !EmailValidatorUtils.isValidEmail(trimmed)) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "以下邮箱格式错误:" + trimmed);
                }
            }
        }
        if (type != null && EmailTypeEnum.getEnumByValue(type) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮件类型错误");
        }
    }

}