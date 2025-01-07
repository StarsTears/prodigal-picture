package com.prodigal.system.manager.auth;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.json.JSONUtil;
import com.prodigal.system.exception.BusinessException;
import com.prodigal.system.exception.ErrorCode;
import com.prodigal.system.manager.auth.model.SpaceUserPermissionConstant;
import com.prodigal.system.model.entity.Picture;
import com.prodigal.system.model.entity.Space;
import com.prodigal.system.model.entity.SpaceUser;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.model.enums.SpaceRoleEnum;
import com.prodigal.system.model.enums.SpaceTypeEnum;
import com.prodigal.system.service.PictureService;
import com.prodigal.system.service.SpaceService;
import com.prodigal.system.service.SpaceUserService;
import com.prodigal.system.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.prodigal.system.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 自定义权限加载接口实现类
 **/
@Component    // 保证此类被 SpringBoot 扫描，完成 Sa-Token 的自定义权限验证扩展
public class StpInterfaceImpl implements StpInterface {
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;
    @Resource
    private SpaceUserService spaceUserService;
    @Resource
    private PictureService pictureService;
    @Resource
    private UserService userService;
    @Resource
    private SpaceService spaceService;

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 判断 loginType 仅对 登录类型是 "space" 的进行权限校验
        if (!StpKit.SPACE_TYPE.equals(loginType)){
            return new ArrayList<>();
        }
        //管理员权限，表示校验通过
        List<String> ADMIN_PERMISSIONS = spaceUserAuthManager.getPermissionsByRole(SpaceRoleEnum.ADMIN.getValue());
        //获取上下文对象
        SpaceUserAuthContext authContext = this.getAuthContextByRequest();
        //若 所有字段都为空，则代表查询公共图库；则返回管理员权限
        if (isAllFieldsNull(authContext)){
            return ADMIN_PERMISSIONS;
        }
        //获取userID
        User loginUser =(User) StpKit.SPACE.getSessionByLoginId(loginId).get(USER_LOGIN_STATE);
        if (loginUser == null){
            throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
        }
        Long userId = loginUser.getId();
        //从上下文中获取 SpaceUser 信息
        SpaceUser spaceUser = authContext.getSpaceUser();
        if (spaceUser!=null){
            return spaceUserAuthManager.getPermissionsByRole(spaceUser.getSpaceRole());
        }
        //如果有 spaceUserId,必然是团队空间,通过数据库查询 spaceUser 对象
        Long spaceUserId = authContext.getSpaceUserId();
        if (spaceUserId!=null) {
            spaceUser = spaceUserService.getById(spaceUserId);
            if (spaceUser == null){
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"未找到空间成员信息···");
            }
            //取出当前登录用户对应的 spaceUser
            SpaceUser loginSpaceUser = spaceUserService.lambdaQuery().eq(SpaceUser::getUserId, userId)
                    .eq(SpaceUser::getSpaceId, spaceUser.getSpaceId())
                    .one();
            if (loginSpaceUser == null){
                return new ArrayList<>();
            }
            //这里会导致管理员在私有空间没有权限,可以在查库处理
            return spaceUserAuthManager.getPermissionsByRole(loginSpaceUser.getSpaceRole());
        }
        //如果没有spaceUserId,尝试通过spaceId 或者 pictureId 获取 space对象处理
        Long spaceId = authContext.getSpaceId();
        if (spaceId == null){
            //如果没有 soaceId ,尝试通过 pictureId 获取 picture 对象和 space对象处理
            Long pictureId = authContext.getPictureId();
            //图片Id 也没有，则默认通过权限校验
            if (pictureId == null){
                return ADMIN_PERMISSIONS;
            }
            Picture picture = pictureService.lambdaQuery().eq(Picture::getId, pictureId)
                    .select(Picture::getId, Picture::getSpaceId, Picture::getUserId)
                    .one();
            if (picture==null){
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"未找到图片信息···");
            }
            spaceId = picture.getSpaceId();
            //公共图库 仅本人或管理员可操作
            if (spaceId == null){
                if (picture.getUserId().equals(userId) || userService.isAdmin(loginUser)){
                    return ADMIN_PERMISSIONS;
                }else{
                    //不是自己的图片只能查看
                    return Collections.singletonList(SpaceUserPermissionConstant.PICTURE_VIEW);
                }
            }
        }
        //获取 space 对象
        Space space = spaceService.getById(spaceId);
        if (space == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"未找到空间信息···");
        }
        //根据spaceType 判断空间类型
       if (space.getSpaceType() == SpaceTypeEnum.PRIVATE.getValue()){
           //私有空间 仅本人或管理员有权限
           if (userId.equals(space.getUserId()) || userService.isAdmin(loginUser)){
               return ADMIN_PERMISSIONS;
           }else{
               //不是自己的空间只能查看
               return new ArrayList<>();
           }
       }else{
           //团队空间,查询spaceUser 并获取角色与权限
           spaceUser = spaceUserService.lambdaQuery()
                   .eq(SpaceUser::getSpaceId, spaceId)
                   .eq(SpaceUser::getUserId, userId)
                   .one();
           if (spaceUser == null){
               return new ArrayList<>();
           }
           return spaceUserAuthManager.getPermissionsByRole(spaceUser.getSpaceRole());
       }
    }


    private boolean isAllFieldsNull(Object object) {
        if (object == null){
            return true;
        }
        //获取所有字段并判断是否所有字段都为空
        return Arrays.stream(ReflectUtil.getFields(object.getClass()))
                //获取字段值
                .map(field -> ReflectUtil.getFieldValue(object, field))
                //判断是否为空
                .allMatch(ObjectUtil::isEmpty);
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // 本 list 仅做模拟，实际项目中要根据具体业务逻辑来查询角色
        List<String> list = new ArrayList<String>();
        list.add("admin");
        list.add("super-admin");
        return list;
    }

    /**
     * 从请求中获取上下文对象
     */
    private SpaceUserAuthContext getAuthContextByRequest() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String contentType = request.getHeader(Header.CONTENT_TYPE.getValue());
        SpaceUserAuthContext authRequest;
        // 兼容 get 和 post 操作
        if (ContentType.JSON.getValue().equals(contentType)) {
            String body = ServletUtil.getBody(request);
            authRequest = JSONUtil.toBean(body, SpaceUserAuthContext.class);
        } else {
            Map<String, String> paramMap = ServletUtil.getParamMap(request);
            authRequest = BeanUtil.toBean(paramMap, SpaceUserAuthContext.class);
        }
        // 根据请求路径区分 id 字段的含义
        Long id = authRequest.getId();
        if (ObjUtil.isNotNull(id)) {
            String requestUri = request.getRequestURI();
            String partUri = requestUri.replace(contextPath + "/", "");
            String moduleName = StrUtil.subBefore(partUri, "/", false);
            switch (moduleName) {
                case "picture":
                    authRequest.setPictureId(id);
                    break;
                case "spaceUser":
                    authRequest.setSpaceUserId(id);
                    break;
                case "space":
                    authRequest.setSpaceId(id);
                    break;
                default:
            }
        }
        return authRequest;
    }
}
