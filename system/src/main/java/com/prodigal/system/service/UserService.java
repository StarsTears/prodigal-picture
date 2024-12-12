package com.prodigal.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.prodigal.system.model.dto.user.LoginDto;
import com.prodigal.system.model.dto.user.RegisterDto;
import com.prodigal.system.model.dto.user.UserQueryDto;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author Lang
* @description 针对表【user(用户)】的数据库操作Service
*/
public interface UserService extends IService<User> {
    long register(RegisterDto registerDto);

    UserVO login(LoginDto loginDto, HttpServletRequest request);

    User getLoginUser(HttpServletRequest request);

    boolean logout(HttpServletRequest request);

    LambdaQueryWrapper<User> getQueryWrapper(UserQueryDto userQueryDto);

    String getEncryptPassword(String password);

    UserVO getUserVO(User user);

    List<UserVO> getUserVOList(List<User> userList);
}
