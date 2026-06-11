package com.prodigal.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.prodigal.system.exception.BusinessException;
import com.prodigal.system.exception.ErrorCode;
import com.prodigal.system.exception.ThrowUtils;
import com.prodigal.system.model.dto.spaceuser.SpaceUserAddDTO;
import com.prodigal.system.model.dto.spaceuser.SpaceUserQueryDTO;
import com.prodigal.system.model.entity.Space;
import com.prodigal.system.model.entity.SpaceUser;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.model.enums.SpaceRoleEnum;
import com.prodigal.system.model.vo.SpaceUserVO;
import com.prodigal.system.model.vo.SpaceVO;
import com.prodigal.system.model.vo.UserVO;
import com.prodigal.system.service.SpaceService;
import com.prodigal.system.service.SpaceUserService;
import com.prodigal.system.mapper.SpaceUserMapper;
import com.prodigal.system.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author Lang
* @description 针对表【space_user(空间用户关联)】的数据库操作Service实现
*/
@Service
public class SpaceUserServiceImpl extends ServiceImpl<SpaceUserMapper, SpaceUser> implements SpaceUserService{
    @Resource
    private UserService userService;
    @Resource
    private SpaceService spaceService;
    @Override
    public void validSpaceUser(SpaceUser spaceUser, boolean add) {
        ThrowUtils.throwIf(spaceUser == null, ErrorCode.PARAMS_ERROR);
        // 创建时，空间 id 和用户 id 必填
        String spaceId = spaceUser.getSpaceId();
        String userId = spaceUser.getUserId();
        if (add) {
            ThrowUtils.throwIf(ObjectUtil.hasEmpty(spaceId, userId), ErrorCode.PARAMS_ERROR);
            User user = userService.getById(userId);
            ThrowUtils.throwIf(user == null, ErrorCode.USER_NOT_FOUND, "用户不存在");
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        }
        // 校验空间角色
        String spaceRole = spaceUser.getSpaceRole();
        SpaceRoleEnum spaceRoleEnum = SpaceRoleEnum.getEnumByValue(spaceRole);
        if (spaceRole != null && spaceRoleEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间角色不存在");
        }
    }

    @Override
    public String addSpaceUser(SpaceUserAddDTO spaceUserAddDTO) {
        // 参数校验
        ThrowUtils.throwIf(spaceUserAddDTO == null, ErrorCode.PARAMS_ERROR);
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(spaceUserAddDTO, spaceUser);
        if (spaceUserAddDTO.getSpaceRole() != null) {
            spaceUser.setSpaceRole(spaceUserAddDTO.getSpaceRole().getValue());
        }
        this.validSpaceUser(spaceUser, true);
        // 数据库操作
        boolean result = this.save(spaceUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return spaceUser.getId();
    }

    @Override
    public SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request) {
        // 对象转封装类
        SpaceUserVO spaceUserVO = SpaceUserVO.objToVO(spaceUser);
        // 关联查询用户信息
        String userId = spaceUser.getUserId();
        if (userId != null && !"0".equals(userId)) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            spaceUserVO.setUser(userVO);
        }
        // 关联查询空间信息
        String spaceId = spaceUser.getSpaceId();
        if (spaceId != null && !"0".equals(spaceId)) {
            Space space = spaceService.getById(spaceId);
            SpaceVO spaceVO = spaceService.getSpaceVO(space, request);
            spaceUserVO.setSpace(spaceVO);
        }
        return spaceUserVO;
    }

    @Override
    public List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList) {
        // 判断输入列表是否为空
        if (CollUtil.isEmpty(spaceUserList)) {
            return Collections.emptyList();
        }
        // 对象列表 => 封装对象列表
        List<SpaceUserVO> spaceUserVOList = spaceUserList.stream().map(SpaceUserVO::objToVO).collect(Collectors.toList());
        // 1. 收集需要关联查询的用户 ID 和空间 ID
        Set<String> userIdSet = spaceUserList.stream().map(SpaceUser::getUserId).collect(Collectors.toSet());
        Set<String> spaceIdSet = spaceUserList.stream().map(SpaceUser::getSpaceId).collect(Collectors.toSet());
        // 2. 批量查询用户和空间
        Map<String, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                                                                .collect(Collectors.groupingBy(User::getId));
        Map<String, List<Space>> spaceIdSpaceListMap = spaceService.listByIds(spaceIdSet).stream()
                                                                .collect(Collectors.groupingBy(Space::getId));
        // 3. 填充 SpaceUserVO 的用户和空间信息
        spaceUserVOList.forEach(spaceUserVO -> {
            String userId = spaceUserVO.getUserId();
            String spaceId = spaceUserVO.getSpaceId();
            // 填充用户信息
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceUserVO.setUser(userService.getUserVO(user));
            // 填充空间信息
            Space space = null;
            if (spaceIdSpaceListMap.containsKey(spaceId)) {
                space = spaceIdSpaceListMap.get(spaceId).get(0);
            }
            spaceUserVO.setSpace(SpaceVO.objToVO(space));
        });
        return spaceUserVOList;
    }


    @Override
    public QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryDTO spaceUserQueryDTO) {
        QueryWrapper<SpaceUser> queryWrapper = new QueryWrapper<>();
        if (spaceUserQueryDTO == null) {
            return queryWrapper;
        }
        // 从对象中取值
        String id = spaceUserQueryDTO.getId();
        String spaceId = spaceUserQueryDTO.getSpaceId();
        String userId = spaceUserQueryDTO.getUserId();
        String spaceRole = spaceUserQueryDTO.getSpaceRole() != null ? spaceUserQueryDTO.getSpaceRole().getValue() : null;
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "space_id", spaceId);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "user_id", userId);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceRole), "space_role", spaceRole);
        return queryWrapper;
    }
}




