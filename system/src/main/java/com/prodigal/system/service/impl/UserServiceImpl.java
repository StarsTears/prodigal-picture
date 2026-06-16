package com.prodigal.system.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.prodigal.system.constant.CacheConstant;
import com.prodigal.system.constant.UserConstant;
import com.prodigal.system.exception.BusinessException;
import com.prodigal.system.exception.ErrorCode;
import com.prodigal.system.exception.ThrowUtils;
import com.prodigal.system.manager.auth.StpKit;
import com.prodigal.system.mapper.UserMapper;
import com.prodigal.system.model.dto.system.LoginDTO;
import com.prodigal.system.model.dto.system.RegisterDTO;
import com.prodigal.system.model.dto.system.ResetPasswordDTO;
import com.prodigal.system.model.dto.user.*;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.model.enums.UserRoleEnum;
import com.prodigal.system.model.vo.UserVO;
import com.prodigal.system.service.EmailService;
import com.prodigal.system.service.UserService;
import com.prodigal.system.utils.EmailValidatorUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final String MD5_SALT = "prodigal";
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Lazy
    @Resource
    private EmailService emailService;

    @Override
    public String register(RegisterDTO registerDto) {
        if (registerDto == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (registerDto.getUserAccount().length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户长度过短!");
        }
        String userEmail = registerDto.getUserEmail();
        if (StrUtil.isBlank(userEmail)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱不能为空!");
        }
        if (!EmailValidatorUtils.isValidEmail(userEmail)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式错误!");
        }
        if (registerDto.getUserPassword().length() < 6 || registerDto.getCheckPassword().length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度过短!");
        }
        if (!registerDto.getUserPassword().equals(registerDto.getCheckPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_NOT_MATCH);
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>();
        wrapper.eq(User::getUserAccount, registerDto.getUserAccount())
                .or()
                .eq(StrUtil.isNotBlank(userEmail), User::getUserEmail, registerDto.getUserEmail());
        Long count = this.baseMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.USER_EXIST);
        }
        String encryptPassword = getEncryptPassword(registerDto.getUserPassword());
        User user = new User();
        user.setUserAccount(registerDto.getUserAccount());
        user.setUserPassword(encryptPassword);
        user.setUserRole(UserRoleEnum.USER.getRole());
        String userName = registerDto.getUserName();
        user.setUserName(StrUtil.isNotBlank(userName) ? userName : user.getUserAccount());
        if (StrUtil.isNotBlank(userEmail)) {
            user.setUserEmail(userEmail);
        }
        user.setShareCode(generateShareCode());
        boolean save = this.save(user);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败!");
        }
        return user.getId();
    }

    @Override
    public UserVO login(LoginDTO loginDto, HttpServletRequest request) {
        String failKey = CacheConstant.LOGIN_FAIL_PREFIX + loginDto.getUserAccount();
        String failCountStr = stringRedisTemplate.opsForValue().get(failKey);
        long failCount = failCountStr != null ? Long.parseLong(failCountStr) : 0;
        if (failCount >= CacheConstant.LOGIN_FAIL_MAX_COUNT) {
            if (StrUtil.isBlank(loginDto.getEmail()) || StrUtil.isBlank(loginDto.getCaptcha())) {
                throw new BusinessException(ErrorCode.CAPTCHA_ERROR, "请先获取验证码");
            }
            boolean valid = emailService.verifyCode(loginDto.getEmail(), loginDto.getCaptcha());
            if (!valid) {
                throw new BusinessException(ErrorCode.CAPTCHA_ERROR);
            }
        }

        User user = this.lambdaQuery()
                .eq(User::getUserAccount, loginDto.getUserAccount())
                .one();
        if (user == null || !matchPassword(loginDto.getUserPassword(), user.getUserPassword())) {
            log.error("user login failed, userAccount cannot match userPassword");
            Long newCount = stringRedisTemplate.opsForValue().increment(failKey);
            if (newCount != null && newCount == 1) {
                stringRedisTemplate.expire(failKey, CacheConstant.LOGIN_FAIL_EXPIRE_MINUTES, TimeUnit.MINUTES);
            }
            throw new BusinessException(ErrorCode.LOGIN_FAIL);
        }

        stringRedisTemplate.delete(failKey);
        // MD5 密码自动升级为 BCrypt
        if (isLegacyHash(user.getUserPassword())) {
            user.setUserPassword(getEncryptPassword(loginDto.getUserPassword()));
            this.updateById(user);
        }
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        StpKit.SPACE.login(user.getId());
        StpKit.SPACE.getSession().set(UserConstant.USER_LOGIN_STATE, user);
        return this.getUserVO(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
        }
        String userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
        }
        return currentUser;
    }

    @Override
    public boolean logout(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.USER_NOT_LOGIN, "未登录!");
        }
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        // 移除当前用户的 Sa-Token 登录态
        User currentUser = (User) userObj;
        if (currentUser.getId() != null) {
            StpKit.SPACE.logout(currentUser.getId());
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String createUser(UserAddDTO userAddDto) {
        ThrowUtils.throwIf(userAddDto == null, ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtils.copyProperties(userAddDto, user);
        String userEmail = userAddDto.getUserEmail();
        if (StrUtil.isBlank(userEmail)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱不能为空!");
        }
        if (StrUtil.isNotBlank(userEmail) && !EmailValidatorUtils.isValidEmail(userEmail)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式错误!");
        }
        //初始密码 123456
        final String DEFAULT_PASSWORD = "123456";
        String encryptPassword = this.getEncryptPassword(DEFAULT_PASSWORD);
        user.setUserPassword(encryptPassword);
        //设置默认角色
        String userRoleStr = userAddDto.getUserRole() != null ? userAddDto.getUserRole().getRole() : UserRoleEnum.USER.getRole();
        user.setUserRole(userRoleStr);
        user.setShareCode(generateShareCode());
        boolean save = this.save(user);
        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR);
        return user.getId();
    }

    @Override
    public LambdaQueryWrapper<User> getQueryWrapper(UserQueryDTO userQueryDto) {
        if (userQueryDto == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空!");
        }
        String sortOrder = userQueryDto.getSortOrder();
        String sortField = userQueryDto.getSortField() == null ? "" : userQueryDto.getSortField().trim();
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(true, User::getIsDelete, 0)
                .eq(ObjUtil.isNotEmpty(userQueryDto.getId()), User::getId, userQueryDto.getId())
                .like(StrUtil.isNotBlank(userQueryDto.getUserName()), User::getUserName, userQueryDto.getUserName())
                .like(StrUtil.isNotBlank(userQueryDto.getUserAccount()), User::getUserAccount, userQueryDto.getUserAccount())
                .like(StrUtil.isNotBlank(userQueryDto.getUserProfile()), User::getUserProfile, userQueryDto.getUserProfile());
        if (userQueryDto.getUserRole() != null) {
            wrapper.eq(User::getUserRole, userQueryDto.getUserRole().getRole());
        }
        switch (sortField) {
            case "user_account":
                wrapper.orderBy(StrUtil.isNotEmpty(userQueryDto.getSortField()), sortOrder.equals("ascend"), User::getCreateTime);
                break;
            case "user_name":
                wrapper.orderBy(StrUtil.isNotEmpty(userQueryDto.getSortField()), sortOrder.equals("ascend"), User::getUserName);
                break;
            case "user_profile":
                wrapper.orderBy(StrUtil.isNotEmpty(userQueryDto.getSortField()), sortOrder.equals("ascend"), User::getUserProfile);
                break;
            case "user_role":
                wrapper.orderBy(StrUtil.isNotEmpty(userQueryDto.getSortField()), sortOrder.equals("ascend"), User::getUserRole);
                break;
            default:
                break;
        }
        return wrapper;
    }

    /**
     * BCrypt 密文以 $2a$、$2b$、$2y$ 开头，用于区分新旧算法
     */
    private boolean isLegacyHash(String hash) {
        return hash != null && !hash.startsWith("$2");
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollectionUtils.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public boolean isAdmin(User user) {
        if (user == null || StrUtil.isBlank(user.getUserRole())) {
            return false;
        }
        List<String> roles = StrUtil.split(user.getUserRole(), ',');
        return roles.contains(UserRoleEnum.ADMIN.getRole()) || roles.contains(UserRoleEnum.ADMINISTRATOR.getRole());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void resetPassword(ResetPasswordDTO dto) {
        String userAccount = dto.getUserAccount();
        String userEmail = dto.getUserEmail();
        String newPassword = dto.getNewPassword();
        String checkPassword = dto.getCheckPassword();

        ThrowUtils.throwIf(!newPassword.equals(checkPassword), ErrorCode.PASSWORD_NOT_MATCH);

        User user = this.lambdaQuery().eq(User::getUserAccount, userAccount).one();
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (!userEmail.equals(user.getUserEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_NOT_MATCH);
        }
        user.setUserPassword(getEncryptPassword(newPassword));
        boolean updated = this.updateById(user);
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "密码重置失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void changePassword(ChangePasswordDTO dto, User loginUser) {
        String newPassword = dto.getNewPassword();
        ThrowUtils.throwIf(!newPassword.equals(dto.getCheckPassword()), ErrorCode.PASSWORD_NOT_MATCH);
        ThrowUtils.throwIf(!matchPassword(dto.getOldPassword(), loginUser.getUserPassword()), ErrorCode.LOGIN_FAIL, "原密码错误");

        User user = this.getById(loginUser.getId());
        user.setUserPassword(getEncryptPassword(newPassword));
        boolean updated = this.updateById(user);
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "密码修改失败");
        }
    }

    @Override
    public String getEncryptPassword(String password) {
        return passwordEncoder.encode(password);
    }

    /**
     * 密码匹配：BCrypt 优先，回退 MD5 兼容存量数据
     */
    private boolean matchPassword(String rawPassword, String storedHash) {
        if (isLegacyHash(storedHash)) {
            String legacyHash = DigestUtils.md5DigestAsHex((MD5_SALT + rawPassword).getBytes());
            return legacyHash.equals(storedHash);
        }
        return passwordEncoder.matches(rawPassword, storedHash);
    }

    private static final String SHARE_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Random RANDOM = new Random();

    /**
     * 生成不重复的分享码（8位大写字母+数字）
     */
    private String generateShareCode() {
        for (int attempt = 0; attempt < 10; attempt++) {
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 8; i++) {
                sb.append(SHARE_CODE_CHARS.charAt(RANDOM.nextInt(SHARE_CODE_CHARS.length())));
            }
            String code = sb.toString();
            if (!this.lambdaQuery().eq(User::getShareCode, code).exists()) {
                return code;
            }
        }
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "分享码生成失败，请重试");
    }
}
