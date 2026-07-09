package com.prodigal.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prodigal.system.model.dto.space.SpaceAddDTO;
import com.prodigal.system.model.dto.space.SpaceEditDTO;
import com.prodigal.system.model.dto.space.SpaceQueryDTO;
import com.prodigal.system.model.entity.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.model.vo.SpaceVO;

import jakarta.servlet.http.HttpServletRequest;

/**
* @author Lang
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2024-12-19 17:28:41
*/
public interface SpaceService extends IService<Space> {

    void validSpace(Space space, boolean add);


    String addSpace(SpaceAddDTO spaceAddDto, User loginUser);

    void editSpace(SpaceEditDTO spaceEditDto, User loginUser);

    void deleteSpace(String spaceId, User loginUser);

    SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    Page<SpaceVO> listSpaceVOeByPage(Page<Space> spacePage, HttpServletRequest request);

    LambdaQueryWrapper<Space> getQueryWrapper(SpaceQueryDTO spaceQueryDto);

    void fillSpaceBySpaceLevel(Space space);


    void checkSpacePermission(Space space,User loginUser);
}
