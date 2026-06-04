package com.prodigal.system.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.prodigal.system.constant.UserConstant;
import com.prodigal.system.exception.BusinessException;
import com.prodigal.system.exception.ErrorCode;
import com.prodigal.system.exception.ThrowUtils;
import com.prodigal.system.manager.auth.StpKit;
import com.prodigal.system.mapper.UserMapper;
import com.prodigal.system.model.dto.user.LoginDto;
import com.prodigal.system.model.dto.user.RegisterDto;
import com.prodigal.system.model.dto.user.ResetPasswordDto;
import com.prodigal.system.model.dto.user.UserQueryDto;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.model.enums.UserRoleEnum;
import com.prodigal.system.model.vo.UserVO;
import com.prodigal.system.service.UserService;
import com.prodigal.system.utils.EmailValidatorUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final String MD5_SALT = "prodigal";
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public long register(RegisterDto registerDto) {
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
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "账户/邮箱已存在!");
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
        boolean save = this.save(user);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败!");
        }
        return user.getId();
    }

    @Override
    public UserVO login(LoginDto loginDto, HttpServletRequest request) {
        if (loginDto == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (loginDto.getUserAccount().length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户错误!");
        }
        if (loginDto.getUserPassword().length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误!");
        }
        User user = this.lambdaQuery()
                .eq(User::getUserAccount, loginDto.getUserAccount())
                .one();
        if (user == null || !matchPassword(loginDto.getUserPassword(), user.getUserPassword())) {
            log.error("user login failed,userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在或密码错误!");
        }
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
        Long userId = currentUser.getId();
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
        return true;
    }

    @Override
    public LambdaQueryWrapper<User> getQueryWrapper(UserQueryDto userQueryDto) {
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
        String userRoleStr = StrUtil.isNotBlank(userQueryDto.getUserRole()) && userQueryDto.getUserRole().startsWith(",") ?
                userQueryDto.getUserRole().substring(1) :
                userQueryDto.getUserRole();
        if (StrUtil.isNotBlank(userRoleStr)) {
            if (userRoleStr.contains(",")) {
                List<String> userRoles = Arrays.stream(userRoleStr.split(",")).collect(Collectors.toList());
                wrapper.and(e -> {
                    for (String userRole : userRoles) {
                        e.eq(User::getUserRole, userRole).or();
                    }
                });
            } else {
                wrapper.eq(User::getUserRole, userRoleStr);
            }
        }
        switch (sortField) {
            case "userAccount":
                wrapper.orderBy(StrUtil.isNotEmpty(userQueryDto.getSortField()), sortOrder.equals("ascend"), User::getCreateTime);
                break;
            case "userName":
                wrapper.orderBy(StrUtil.isNotEmpty(userQueryDto.getSortField()), sortOrder.equals("ascend"), User::getUserName);
                break;
            case "userProfile":
                wrapper.orderBy(StrUtil.isNotEmpty(userQueryDto.getSortField()), sortOrder.equals("ascend"), User::getUserProfile);
                break;
            case "userRole":
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
        return user != null && (user.getUserRole().contains(UserConstant.ADMIN_ROLE) || user.getUserRole().contains(UserConstant.SUPER_ADMIN_ROLE));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void resetPassword(ResetPasswordDto dto) {
        String userAccount = dto.getUserAccount();
        String userEmail = dto.getUserEmail();
        String newPassword = dto.getNewPassword();
        String checkPassword = dto.getCheckPassword();

        ThrowUtils.throwIf(!newPassword.equals(checkPassword), ErrorCode.PASSWORD_NOT_MATCH);

        User user = this.lambdaQuery().eq(User::getUserAccount, userAccount).one();
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "账户不存在");
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
}
