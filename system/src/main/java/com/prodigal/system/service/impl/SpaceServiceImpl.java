package com.prodigal.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.prodigal.system.exception.BusinessException;
import com.prodigal.system.exception.ErrorCode;
import com.prodigal.system.exception.ThrowUtils;
import com.prodigal.system.manager.sharding.DynamicShardingManager;
import com.prodigal.system.model.dto.space.SpaceAddDto;
import com.prodigal.system.model.dto.space.SpaceEditDto;
import com.prodigal.system.model.dto.space.SpaceQueryDto;
import com.prodigal.system.model.entity.Picture;
import com.prodigal.system.model.entity.Space;
import com.prodigal.system.model.entity.SpaceUser;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.model.enums.SpaceLevelEnum;
import com.prodigal.system.model.enums.SpaceRoleEnum;
import com.prodigal.system.model.enums.SpaceTypeEnum;
import com.prodigal.system.model.vo.SpaceVO;
import com.prodigal.system.model.vo.UserVO;
import com.prodigal.system.service.PictureService;
import com.prodigal.system.service.SpaceService;
import com.prodigal.system.mapper.SpaceMapper;
import com.prodigal.system.service.SpaceUserService;
import com.prodigal.system.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Lang
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2024-12-19 17:28:41
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space> implements SpaceService {
    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private PictureService pictureService;

    @Resource
    @Lazy
    private SpaceUserService spaceUserService;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    @Lazy
    private DynamicShardingManager dynamicShardingManager;
    private Map<Long, Object> lockMap = new ConcurrentHashMap<>();

    /**
     * 校验
     *
     * @param space 空间信息
     * @param add   是否为新增
     */
    @Override
    public void validSpace(Space space, boolean add) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        Integer spaceType = space.getSpaceType();
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(spaceType);
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        // 要创建
        if (add) {
            if (StrUtil.isBlank(spaceName)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称不能为空");
            }
            if (spaceLevel == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不能为空");
            }
            //对空间类型进行校验
            if (spaceType == null){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间类型不能为空");
            }
        }
        // 修改数据时，如果要改空间级别
        if (spaceLevel != null && spaceLevelEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不存在");
        }
        if (StrUtil.isNotBlank(spaceName) && spaceName.length() > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称过长");
        }
        //修改数据时，如果要更改空间类型
        if (spaceType!=null && spaceTypeEnum == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间类型不存在");
        }
    }

    @Override
    public long addSpace(SpaceAddDto spaceAddDto, User loginUser) {
        ThrowUtils.throwIf(spaceAddDto == null, ErrorCode.PARAMS_ERROR);

        //空间名称为空，给其赋默认值
        if (StrUtil.isBlank(spaceAddDto.getSpaceName())) {
            spaceAddDto.setSpaceName("默认空间");
        }
        if (spaceAddDto.getSpaceLevel() == null) {
            spaceAddDto.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        if (spaceAddDto.getSpaceType() == null) {
            spaceAddDto.setSpaceType(SpaceTypeEnum.PRIVATE.getValue());
        }
        Space space = new Space();
        BeanUtils.copyProperties(spaceAddDto, space);
        //填充数据
        this.fillSpaceBySpaceLevel(space);
        //校验
        this.validSpace(space, true);
        Long userId = loginUser.getId();
        space.setUserId(userId);
        //权限校验
        if (space.getSpaceLevel() != SpaceLevelEnum.COMMON.getValue() && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.USER_NOT_PERMISSION, "无权限创建指定级别的空间");
        }
        Object lock = lockMap.computeIfAbsent(userId, key -> new Object());
        synchronized (lock) {
            Long newSpaceId = transactionTemplate.execute(status -> {
                try {
                    //判断该空间是否存在
                    boolean exists = this.lambdaQuery()
                                            .eq(Space::getUserId, userId)
                                            .eq(Space::getSpaceType, space.getSpaceType())
                                            .exists();
                    ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "每个用户每类空间只能创建一个！！！");
                    //保存到数据库
                    boolean result = this.save(space);
                    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
                    //如果是团队空间，则新增一条 空间成员信息
                    if (SpaceTypeEnum.TEAM.getValue() == spaceAddDto.getSpaceType()){
                        SpaceUser spaceUser = new SpaceUser();
                        spaceUser.setUserId(userId);
                        spaceUser.setSpaceId(space.getId());
                        spaceUser.setSpaceRole(SpaceRoleEnum.ADMIN.getValue());
                        result = spaceUserService.save(spaceUser);
                        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR,"创建团队成员记录失败···");
                    }
                    //创建分表
                    dynamicShardingManager.createSpacePictureTable(space);
                    return space.getId();
                } finally {
                    //释放锁、防止内存泄露
                    lockMap.remove(userId);
                }
            });
            return Optional.ofNullable(newSpaceId).orElse(-1L);
        }
    }

    @Override
    public void editSpace(SpaceEditDto spaceEditDto, User loginUser) {
        Space space = new Space();
        BeanUtils.copyProperties(spaceEditDto, space);
        //自动填充数据
        this.fillSpaceBySpaceLevel(space);
        //设置编辑时间
        space.setEditTime(new Date());
        //数据校验
        this.validSpace(space, false);
        //判断空间是否存在
        Long id = spaceEditDto.getId();
        Space oldSpace = this.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        //验证权限
        //图片应只能管理员/本人删除
        if (!loginUser.getId().equals(oldSpace.getUserId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.USER_NOT_PERMISSION);
        }
        boolean result = this.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    @Override
    public void deleteSpace(Long spaceId, User loginUser) {
        //图片应只能管理员/本人删除
        //查询该图片是否存在
        Space space = this.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        //验证权限
        if (!loginUser.getId().equals(space.getUserId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.USER_NOT_PERMISSION);
        }
        //删除图片开启事物、删除成功后释放额度
        transactionTemplate.execute(status -> {
            //删除该空间内的所有图片
            List<Picture> pictureList = pictureService.lambdaQuery()
                                                        .eq(Picture::getSpaceId, spaceId)
                                                        .select(Picture::getId).list();
            if (CollUtil.isNotEmpty(pictureList)) {
                List<Long> pictureIds = pictureList.stream().map(Picture::getId).collect(Collectors.toList());
                boolean result = pictureService.removeByIds(pictureIds);
                ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
            }
        boolean result = this.removeById(spaceId);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
            return true;
        });
    }

    @Override
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
        //根据Picture信息 脱敏后返回给前端
        SpaceVO spaceVO = SpaceVO.objToVO(space);
        //根据userID 获取UserVO
        Long userId = space.getUserId();
        if (userId != null && userId > 0) {
            UserVO userVO = userService.getUserVO(userService.getById(userId));
            spaceVO.setUser(userVO);
        }
        return spaceVO;
    }

    @Override
    public Page<SpaceVO> listSpaceVOeByPage(Page<Space> spacePage, HttpServletRequest request) {
        List<Space> spaceList = spacePage.getRecords();
        Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        if (CollUtil.isEmpty(spaceList)) {
            return spaceVOPage;
        }
        //转换VO
        List<SpaceVO> pictureVOList = spaceList.stream().map(SpaceVO::objToVO).collect(Collectors.toList());
        //关联查询用户信息
        Set<Long> userIDSet = spaceList.stream().map(Space::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIDUserListMap = userService.listByIds(userIDSet).stream().collect(Collectors.groupingBy(User::getId));
        pictureVOList.forEach(e -> {
            Long userId = e.getUserId();
            if (userIDUserListMap.containsKey(userId))
                e.setUser(userService.getUserVO(userIDUserListMap.get(userId).get(0)));
        });
        spaceVOPage.setRecords(pictureVOList);
        return spaceVOPage;
    }


    /**
     * 分页查询参数封装
     *
     * @param spaceQueryDto 图片查询参数
     */
    @Override
    public LambdaQueryWrapper<Space> getQueryWrapper(SpaceQueryDto spaceQueryDto) {
        LambdaQueryWrapper<Space> wrapper = new LambdaQueryWrapper<Space>();
        if (spaceQueryDto == null) {
            return wrapper;
        }
        String sortOrder = spaceQueryDto.getSortOrder();
        String sortField = spaceQueryDto.getSortField() == null ? "" : spaceQueryDto.getSortField().trim();
        wrapper.eq(true, Space::getIsDelete, 0)
                .eq(ObjUtil.isNotEmpty(spaceQueryDto.getId()), Space::getId, spaceQueryDto.getId())
                .eq(ObjUtil.isNotEmpty(spaceQueryDto.getUserId()), Space::getUserId, spaceQueryDto.getUserId())
                .eq(ObjUtil.isNotEmpty(spaceQueryDto.getSpaceType()), Space::getSpaceType, spaceQueryDto.getSpaceType())
                .eq(StrUtil.isNotBlank(spaceQueryDto.getSpaceName()), Space::getSpaceName, spaceQueryDto.getSpaceName())
                .eq(ObjUtil.isNotEmpty(spaceQueryDto.getSpaceLevel()), Space::getSpaceLevel, spaceQueryDto.getSpaceLevel());

        switch (sortField) {
            case "spaceName":
                wrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), Space::getSpaceName);
                break;
            case "spaceType":
                wrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), Space::getSpaceType);
                break;
            case "spaceLevel":
                wrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), Space::getSpaceLevel);
                break;
            case "createTime":
                wrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), Space::getCreateTime);
            case "editTime":
                wrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), Space::getEditTime);
                break;
            default:
                break;
        }
        return wrapper;
    }


    /**
     * 自动填充限额
     *
     * @param space
     */
    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        //根据空间级别，自动填充限额
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if (spaceLevelEnum != null) {
            long maxSize = spaceLevelEnum.getMaxSize();
            if (space.getMaxSize() == null)
                space.setMaxSize(maxSize);

            long maxCount = spaceLevelEnum.getMaxCount();
            if (space.getMaxCount() == null)
                space.setMaxCount(maxCount);
        }
    }

    /**
     * 校验当前用户是否有权限操作空间信息
     * @param loginUser 当前操作用户
     * @param space  空间信息
     */
    @Override
    public void checkSpacePermission(Space space,User loginUser) {
        //只有该空间的所有人 才能修改该空间的数据
        if (!space.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.USER_NOT_PERMISSION,"没有空间访问权限");
        }
    }

}




