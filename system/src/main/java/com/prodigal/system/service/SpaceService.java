package com.prodigal.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prodigal.system.model.dto.space.SpaceAddDto;
import com.prodigal.system.model.dto.space.SpaceEditDto;
import com.prodigal.system.model.dto.space.SpaceQueryDto;
import com.prodigal.system.model.entity.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.model.vo.SpaceVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author Lang
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2024-12-19 17:28:41
*/
public interface SpaceService extends IService<Space> {

    void validSpace(Space space, boolean add);


    long addSpace(SpaceAddDto spaceAddDto, User loginUser);

    void editSpace(SpaceEditDto spaceEditDto, User loginUser);

    void deleteSpace(Long spaceId, User loginUser);

    SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    Page<SpaceVO> listSpaceVOeByPage(Page<Space> spacePage, HttpServletRequest request);

    LambdaQueryWrapper<Space> getQueryWrapper(SpaceQueryDto spaceQueryDto);

    void fillSpaceBySpaceLevel(Space space);


}
