package com.prodigal.system.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.prodigal.system.model.dto.spaceuser.SpaceUserAddDTO;
import com.prodigal.system.model.dto.spaceuser.SpaceUserQueryDTO;
import com.prodigal.system.model.entity.SpaceUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.prodigal.system.model.vo.SpaceUserVO;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author Lang
* @description 针对表【space_user(空间用户关联)】的数据库操作Service
*/
public interface SpaceUserService extends IService<SpaceUser> {

    void validSpaceUser(SpaceUser spaceUser, boolean add);

    String addSpaceUser(SpaceUserAddDTO spaceUserAddDTO);

    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request);

    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);

    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryDTO spaceUserQueryDTO);
}
