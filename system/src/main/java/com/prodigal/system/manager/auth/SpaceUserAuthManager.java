package com.prodigal.system.manager.auth;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.prodigal.system.manager.auth.model.SpaceUserAuthConfig;
import com.prodigal.system.manager.auth.model.SpaceUserRole;
import com.prodigal.system.model.entity.Space;
import com.prodigal.system.model.entity.SpaceUser;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.model.enums.SpaceRoleEnum;
import com.prodigal.system.model.enums.SpaceTypeEnum;
import com.prodigal.system.service.SpaceUserService;
import com.prodigal.system.service.UserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 空间成员权限管理
 **/
@Component
public class SpaceUserAuthManager {
    @Resource
    private SpaceUserService spaceUserService;
    @Resource
    private UserService userService;
    public static final SpaceUserAuthConfig SPACE_USER_AUTH_CONFIG;
    static {
        String json = ResourceUtil.readUtf8Str("biz/spaceUserAuthConfig.json");
        SPACE_USER_AUTH_CONFIG = JSONUtil.toBean(json, SpaceUserAuthConfig.class);
    }

    /**
     * 根据角色获取权限列表
     * @param spaceUserRole
     * @return
     */
    public List<String>  getPermissionsByRole(String spaceUserRole) {
        if (StrUtil.isBlank(spaceUserRole)){
            return new ArrayList<>();
        }

        SpaceUserRole role = SPACE_USER_AUTH_CONFIG.getRoles().stream()
                                                    .filter(r -> spaceUserRole.equals(r.getKey()))
                                                    .findFirst()
                                                    .orElse(null);
        if (null == role){
            return new ArrayList<>();
        }
        return role.getPermissions();
    }

    public List<String> getPermissionList(Space space, User loginUser) {
        if (loginUser == null) {
            return new ArrayList<>();
        }
        // 管理员权限
        List<String> ADMIN_PERMISSIONS = getPermissionsByRole(SpaceRoleEnum.ADMIN.getValue());
        // 公共图库
        if (space == null) {
            if (userService.isAdmin(loginUser)) {
                return ADMIN_PERMISSIONS;
            }
            return new ArrayList<>();
        }
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(space.getSpaceType());
        if (spaceTypeEnum == null) {
            return new ArrayList<>();
        }
        // 根据空间获取对应的权限
        switch (spaceTypeEnum) {
            case PRIVATE:
                // 私有空间，仅本人或管理员有所有权限
                if (space.getUserId().equals(loginUser.getId()) || userService.isAdmin(loginUser)) {
                    return ADMIN_PERMISSIONS;
                } else {
                    return new ArrayList<>();
                }
            case TEAM:
                // 团队空间，查询 SpaceUser 并获取角色和权限
                SpaceUser spaceUser = spaceUserService.lambdaQuery()
                        .eq(SpaceUser::getSpaceId, space.getId())
                        .eq(SpaceUser::getUserId, loginUser.getId())
                        .one();
                if (spaceUser == null) {
                    return new ArrayList<>();
                } else {
                    return getPermissionsByRole(spaceUser.getSpaceRole());
                }
        }
        return new ArrayList<>();
    }
}
