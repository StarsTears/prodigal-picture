package com.prodigal.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.prodigal.system.model.dto.system.LoginDTO;
import com.prodigal.system.model.dto.system.RegisterDTO;
import com.prodigal.system.model.dto.system.ResetPasswordDTO;
import com.prodigal.system.model.dto.user.*;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.model.vo.UserVO;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author Lang
* @description 针对表【user(用户)】的数据库操作Service
*/
public interface UserService extends IService<User> {
    String register(RegisterDTO registerDto);

    UserVO login(LoginDTO loginDto, HttpServletRequest request);

    User getLoginUser(HttpServletRequest request);

    boolean logout(HttpServletRequest request);

    String createUser(UserAddDTO userAddDto);

    LambdaQueryWrapper<User> getQueryWrapper(UserQueryDTO userQueryDto);

    String getEncryptPassword(String password);

    UserVO getUserVO(User user);

    List<UserVO> getUserVOList(List<User> userList);

    boolean isAdmin(User user);

    void resetPassword(ResetPasswordDTO dto);

    void changePassword(ChangePasswordDTO dto, User loginUser);
}
