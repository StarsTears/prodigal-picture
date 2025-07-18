package com.prodigal.system.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.prodigal.system.constant.UserConstant;
import com.prodigal.system.exception.BusinessException;
import com.prodigal.system.exception.ErrorCode;
import com.prodigal.system.manager.auth.StpKit;
import com.prodigal.system.mapper.UserMapper;
import com.prodigal.system.model.dto.user.LoginDto;
import com.prodigal.system.model.dto.user.RegisterDto;
import com.prodigal.system.model.dto.user.UserQueryDto;
import com.prodigal.system.model.entity.Picture;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.model.enums.UserRoleEnum;
import com.prodigal.system.model.vo.UserVO;
import com.prodigal.system.service.UserService;
import com.prodigal.system.utils.EmailValidatorUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lang
 * @description 针对表【user(用户)】的数据库操作Service实现
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    /**
     * 注册
     *
     * @param registerDto 注册参数
     * @return 用户ID
     */
    @Override
    public long register(RegisterDto registerDto) {
        //参数校验
        if (registerDto == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (registerDto.getUserAccount().length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户长度过短!");
        }
        String userEmail = registerDto.getUserEmail();
        if (StrUtil.isBlank(userEmail)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱不能为空!");
        }
        if (StrUtil.isNotBlank(userEmail) && !EmailValidatorUtils.isValidEmail(userEmail)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式错误!");
        }
        if (registerDto.getUserPassword().length() < 6 || registerDto.getCheckPassword().length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度过短!");
        }
        if (!registerDto.getUserPassword().equals(registerDto.getCheckPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码不一致!");
        }
        //查询账户是否重复
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>();
        wrapper.eq(User::getUserAccount, registerDto.getUserAccount())
                .or()
                .eq(StrUtil.isNotBlank(userEmail),User::getUserEmail, registerDto.getUserEmail());
        Long count = this.baseMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "账户已存在!");
        }
        //密码加密存储
        String encryptPassword = getEncryptPassword(registerDto.getUserPassword());
        //插入数据
        User user = new User();
        user.setUserAccount(registerDto.getUserAccount());
        user.setUserPassword(encryptPassword);
        user.setUserRole(UserRoleEnum.USER.getRole());
        String userName = registerDto.getUserName();
        user.setUserName(StrUtil.isNotBlank(userName)?userName:user.getUserAccount());
        if (StrUtil.isNotBlank(userEmail)){
            user.setUserEmail(userEmail);
        }

        boolean save = this.save(user);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败!");
        }
        return user.getId();
    }

    /**
     * 用户登录
     *
     * @param loginDto 登录参数
     * @param request  请求
     * @return 用户信息（脱敏）
     */
    @Override
    public UserVO login(LoginDto loginDto, HttpServletRequest request) {
        //参数校验
        if (loginDto == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (loginDto.getUserAccount().length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户错误!");
        }
        if (loginDto.getUserPassword().length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误!");
        }
        //密码加密
        String encryptPassword = getEncryptPassword(loginDto.getUserPassword());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserAccount, loginDto.getUserAccount())
                .eq(User::getUserPassword, encryptPassword);
        User user = this.baseMapper.selectOne(wrapper);
        if (user == null) {
            log.error("user login failed,userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在或密码错误!");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        // 4. 记录用户登录态到 Sa-token，便于空间鉴权时使用，注意保证该用户信息与 SpringSession 中的信息过期时间一致
        StpKit.SPACE.login(user.getId());
        StpKit.SPACE.getSession().set(UserConstant.USER_LOGIN_STATE, user);
        return this.getUserVO(user);
    }
    /**
     * 获取登录用户信息
     *
     * @param request 请求
     * @return 用户信息
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        //判断是否登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
        }
        //可以查遍数据库在返回；追求性能可直接返回上述结果
        Long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
        }

        return currentUser;
    }

    /**
     * 退出登录
     *
     * @param request 请求
     * @return 结果
     */
    @Override
    public boolean logout(HttpServletRequest request) {
        //先判断是否登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.USER_NOT_LOGIN, "未登录!");
        }
        //移除登录标志
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
        //查询角色
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
            }else{
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
     * 密码加密
     *
     * @param password 密码
     * @return 密文
     */
    @Override
    public String getEncryptPassword(String password) {
        final String salt = "prodigal";
        return DigestUtils.md5DigestAsHex((salt + password).getBytes());
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

    /**
     * 判断用户是否为管理员
     *
     * @param user 用户
     */
    @Override
    public boolean isAdmin(User user) {
        return user != null && (user.getUserRole().contains(UserConstant.ADMIN_ROLE) || user.getUserRole().contains(UserConstant.SUPER_ADMIN_ROLE));
    }

}




