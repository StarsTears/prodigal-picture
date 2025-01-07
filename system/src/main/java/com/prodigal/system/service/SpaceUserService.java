package com.prodigal.system.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.prodigal.system.model.dto.spaceuser.SpaceUserAddDto;
import com.prodigal.system.model.dto.spaceuser.SpaceUserQueryDto;
import com.prodigal.system.model.entity.SpaceUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.prodigal.system.model.vo.SpaceUserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author Lang
* @description 针对表【space_user(空间用户关联)】的数据库操作Service
*/
public interface SpaceUserService extends IService<SpaceUser> {

    void validSpaceUser(SpaceUser spaceUser, boolean add);

    long addSpaceUser(SpaceUserAddDto spaceUserAddDto);

    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request);

    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);

    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryDto spaceUserQueryDto);
}
