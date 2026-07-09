package com.prodigal.system.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.prodigal.system.exception.BizStatus;
import com.prodigal.system.exception.BusinessException;
import com.prodigal.system.exception.ThrowUtils;
import com.prodigal.system.mapper.SpaceMapper;
import com.prodigal.system.model.dto.space.analyze.*;
import com.prodigal.system.model.entity.Picture;
import com.prodigal.system.model.entity.Space;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.model.vo.space.analyze.*;
import com.prodigal.system.service.PictureService;
import com.prodigal.system.service.SpaceAnalyzeService;
import com.prodigal.system.service.SpaceService;
import com.prodigal.system.service.UserService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 图库空间分析实现类
 **/
@Service
public class SpaceAnalyzeServiceImpl extends ServiceImpl<SpaceMapper, Space> implements SpaceAnalyzeService {
    @Resource
    private UserService userService;
    @Resource
    private SpaceService spaceService;
    @Resource
    private PictureService pictureService;

    /**
     * 分析空间使用情况
     *
     * @param spaceUsageAnalyzeDto 空间使用请求参数
     * @param loginUser            登录用户
     * @return
     */
    @Override
    public SpaceUsageAnalyzeVO analyzeSpaceUsage(SpaceUsageAnalyzeDTO spaceUsageAnalyzeDto, User loginUser) {
        ThrowUtils.throwIf(spaceUsageAnalyzeDto == null, BizStatus.PARAMS_ERROR);
        //查询公共空间 权限校验：仅管理员可访问
        //查询全控件/公共空间，需从 Picture 表查询
        if (spaceUsageAnalyzeDto.isQueryAll() || spaceUsageAnalyzeDto.isQueryPublic()) {
            this.checkSpaceAnalyzeAuth(spaceUsageAnalyzeDto, loginUser);
            //统计图库的使用空间
            QueryWrapper<Picture> wrapper = new QueryWrapper<>();
            wrapper.select("pic_size");
            //补充查询范围
            this.fillAnalyzeQueryWrapper(spaceUsageAnalyzeDto, wrapper);
            List<Object> pictureObjList = pictureService.getBaseMapper().selectObjs(wrapper);
            long usedSize = pictureObjList.stream().mapToLong(obj -> (Long) obj).sum();
            long usedCount = pictureObjList.size();

            //封装返回结果
            SpaceUsageAnalyzeVO spaceUsageAnalyzeVO = new SpaceUsageAnalyzeVO();
            spaceUsageAnalyzeVO.setUsedSize(usedSize);
            spaceUsageAnalyzeVO.setMaxSize(null);
            //直接返回给前端 百分比
            spaceUsageAnalyzeVO.setSizeUsageRatio(null);

            spaceUsageAnalyzeVO.setUsedCount(usedCount);
            spaceUsageAnalyzeVO.setMaxCount(null);
            spaceUsageAnalyzeVO.setCountUsageRatio(null);
            return spaceUsageAnalyzeVO;
        } else {
            //查询私有空间 权限校验：仅登录用户可访问
            String spaceId = spaceUsageAnalyzeDto.getSpaceId();
            ThrowUtils.throwIf(spaceId == null || "0".equals(spaceId), BizStatus.PARAMS_ERROR);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, BizStatus.NOT_FOUND_ERROR, "空间不存在");
            //校验权限:仅空间所有者或管理员可访问
            spaceService.checkSpacePermission(space, loginUser);
            //构造返回结果
            SpaceUsageAnalyzeVO spaceUsageAnalyzeVO = new SpaceUsageAnalyzeVO();
            spaceUsageAnalyzeVO.setUsedSize(space.getTotalSize());
            spaceUsageAnalyzeVO.setMaxSize(space.getMaxSize());
            double sizeUsageRatio = (space.getMaxSize() != null && space.getMaxSize() > 0)
                    ? NumberUtil.round(space.getTotalSize() * 100.0 / space.getMaxSize(), 2).doubleValue()
                    : 0;
            spaceUsageAnalyzeVO.setSizeUsageRatio(sizeUsageRatio);

            spaceUsageAnalyzeVO.setUsedCount(space.getTotalCount());
            spaceUsageAnalyzeVO.setMaxCount(space.getMaxCount());
            double countUsageRatio = (space.getMaxCount() != null && space.getMaxCount() > 0)
                    ? NumberUtil.round(space.getTotalCount() * 100.0 / space.getMaxCount(), 2).doubleValue()
                    : 0;
            spaceUsageAnalyzeVO.setCountUsageRatio(countUsageRatio);
            return spaceUsageAnalyzeVO;
        }
    }

    /**
     * 空间图库分类分析
     *
     * @param spaceCategoryAnalyzeDto 分类分析请求参数
     * @param loginUser               登录用户
     */
    @Override
    public List<SpaceCategoryAnalyzeVO> analyzeSpaceCategory(SpaceCategoryAnalyzeDTO spaceCategoryAnalyzeDto, User loginUser) {
        ThrowUtils.throwIf(spaceCategoryAnalyzeDto == null, BizStatus.PARAMS_ERROR);
        //校验权限
        this.checkSpaceAnalyzeAuth(spaceCategoryAnalyzeDto, loginUser);
        //构造查询条件
//        QueryWrapper<Picture> wrapper = Wrappers.lambdaQuery();
        QueryWrapper<Picture> wrapper = new QueryWrapper<>();
        this.fillAnalyzeQueryWrapper(spaceCategoryAnalyzeDto, wrapper);
        wrapper.select("category","count(*) as count", "sum(pic_size) as totalSize")
                .groupBy("category");


        return pictureService.getBaseMapper().selectMaps(wrapper)
                .stream()
                .map(result -> {
                    String category = result.get("category") != null ? result.get("category").toString() : "未分类";
                    Long count = ((Number) result.get("count")).longValue();
                    Long totalSize = ((Number) result.get("totalSize")).longValue();
                    return new SpaceCategoryAnalyzeVO(category, count, totalSize);
                }).collect(Collectors.toList());
    }

    /**
     * 空间图片标签分析
     *
     * @param spaceTagAnalyzeDto 标签分析请求参数
     * @param loginUser          登录用户
     * @return
     */
    @Override
    public List<SpaceTagAnalyzeVO> analyzeSpaceTag(SpaceTagAnalyzeDTO spaceTagAnalyzeDto, User loginUser) {
        ThrowUtils.throwIf(spaceTagAnalyzeDto == null, BizStatus.PARAMS_ERROR);
        //校验权限
        this.checkSpaceAnalyzeAuth(spaceTagAnalyzeDto, loginUser);
        //构造查询条件
        QueryWrapper<Picture> wrapper = new QueryWrapper<>();
        this.fillAnalyzeQueryWrapper(spaceTagAnalyzeDto, wrapper);
        wrapper.select("tags");
        //获取每个图片的标签 ，将其放在一个集合里
        List<String> tagList = pictureService.getBaseMapper().selectObjs(wrapper).stream().filter(Objects::nonNull).map(Object::toString).collect(Collectors.toList());
        //图片的标签可能重复，现在就需要查出集合中每个标签出现的次数
        Map<String, Long> tagCountMap = tagList.stream().flatMap(tag -> JSONUtil.toList(tag, String.class).stream()).collect(Collectors.groupingBy(tag -> tag, Collectors.counting()));
        //将其转换为响应对象，降序排列
        return tagCountMap.entrySet().stream()
                .sorted((t1, t2) -> Long.compare(t2.getValue(), t1.getValue()))
                .map(entry -> new SpaceTagAnalyzeVO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * 空间图片大小分析
     *
     * @param spaceSizeAnalyzeDto 大小分析请求参数
     * @param loginUser           登录用户
     * @return
     */
    @Override
    public List<SpaceSizeAnalyzeVO> analyzeSpaceSize(SpaceSizeAnalyzeDTO spaceSizeAnalyzeDto, User loginUser) {
        ThrowUtils.throwIf(spaceSizeAnalyzeDto == null, BizStatus.PARAMS_ERROR);
        //校验权限
        this.checkSpaceAnalyzeAuth(spaceSizeAnalyzeDto, loginUser);
        //构造查询条件
        QueryWrapper<Picture> wrapper = new QueryWrapper<>();
        this.fillAnalyzeQueryWrapper(spaceSizeAnalyzeDto, wrapper);

        //查询符合条件的图片大小
        wrapper.select("pic_size");
        List<Long> sizeList = pictureService.getBaseMapper().selectObjs(wrapper)
                .stream()
                .map(obj -> ((Number) obj).longValue())
                .collect(Collectors.toList());
        //定义分段范围,使用有序Map LinkedHashMao
        Map<String, Long> sizeRangeMap = new LinkedHashMap<>();
        sizeRangeMap.put("<100KB", sizeList.stream().filter(size -> size < 100 * 1024).count());
        sizeRangeMap.put("100KB-500KB", sizeList.stream().filter(size -> size >= 100 * 1024 && size < 500 * 1024).count());
        sizeRangeMap.put("500KB-1MB", sizeList.stream().filter(size -> size >= 500 * 1024 && size < 1024 * 1024).count());
        sizeRangeMap.put("1MB-2MB", sizeList.stream().filter(size -> size >= 1024 * 1024 && size < 2 * 1024 * 1024).count());
        sizeRangeMap.put(">2MB", sizeList.stream().filter(size -> size >= 2 * 1024 * 1024).count());
        return sizeRangeMap.entrySet().stream()
                .map(entry -> new SpaceSizeAnalyzeVO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * 用户上传行为分析
     * @param spaceUserAnalyzeDto 用户上传行为分析请求参数
     * @param loginUser 登录用户
     * @return
     */
    @Override
    public List<SpaceUserAnalyzeVO> analyzeSpaceUser(SpaceUserAnalyzeDTO spaceUserAnalyzeDto, User loginUser) {
        ThrowUtils.throwIf(spaceUserAnalyzeDto == null, BizStatus.PARAMS_ERROR);
        //检查权限
        this.checkSpaceAnalyzeAuth(spaceUserAnalyzeDto, loginUser);
        //构造查询条件
        QueryWrapper<Picture> wrapper = new QueryWrapper<>();
        String userId = spaceUserAnalyzeDto.getUserId();
        wrapper.eq(ObjUtil.isNotNull(userId), "user_id", userId);
        this.fillAnalyzeQueryWrapper(spaceUserAnalyzeDto, wrapper);
        //时间维度
        String timeDimension = spaceUserAnalyzeDto.getTimeDimension();
        switch (timeDimension) {
            case "day":
                wrapper.select("DATE_FORMAT(create_time, '%Y-%m-%d') as timeRange", "count(*) as count");
                break;
                case "week":
//                  wrapper.select("DATE_FORMAT(create_time, '%Y-%u') as timeRange", "count(*) as count");
                  wrapper.select("YEARWEEK(create_time) as timeRange", "count(*) as count");
                    break;
            case "month":
                wrapper.select("DATE_FORMAT(create_time, '%Y-%m') as timeRange", "count(*) as count");
                break;
            case "year":
                wrapper.select("DATE_FORMAT(create_time, '%Y') as timeRange", "count(*) as count");
                break;
            default:
                throw new BusinessException(BizStatus.PARAMS_ERROR, "不支持的时间维度："+ timeDimension);
        }
        //分组排序
        wrapper.groupBy("timeRange").orderByAsc("timeRange");
        List<Map<String, Object>> pictureListMap = pictureService.getBaseMapper().selectMaps(wrapper);
        return pictureListMap.stream().map(result->{
                                                    String timeRange = result.get("timeRange").toString();
                                                    Long count = ((Number) result.get("count")).longValue();
                                                    return new SpaceUserAnalyzeVO(timeRange, count);
                                            }).collect(Collectors.toList());
    }

    /**
     * 空间排行分析
     * @param spaceRankAnalyzeDto 空间排行分析请求参数
     * @param loginUser 登录用户
     * @return
     */
    @Override
    public List<Space> analyzeSpaceRank(SpaceRankAnalyzeDTO spaceRankAnalyzeDto, User loginUser) {
        ThrowUtils.throwIf(spaceRankAnalyzeDto == null, BizStatus.PARAMS_ERROR);
        //仅管理员可查看空间排行
        ThrowUtils.throwIf(!userService.isAdmin(loginUser), BizStatus.USER_NOT_PERMISSION, "无权访问空间排行");
        //构造查询条件
        QueryWrapper<Space> wrapper = new QueryWrapper<>();
        wrapper.select("id", "space_name", "space_level","total_size", "total_count")
                        .orderByDesc("total_size")
                        .last("LIMIT "+ spaceRankAnalyzeDto.getTopN()); //取前N 名
        return spaceService.list(wrapper);
    }


    /**
     * 校验空间分析权限
     *
     * @param spaceAnalyzeDto 空间请求参数
     * @param loginUser       登录用户
     */
    private void checkSpaceAnalyzeAuth(SpaceAnalyzeDTO spaceAnalyzeDto, User loginUser) {
        // 检查权限
        if (spaceAnalyzeDto.isQueryAll() || spaceAnalyzeDto.isQueryPublic()) {
            // 全空间分析或者公共图库权限校验：仅管理员可访问
            ThrowUtils.throwIf(!userService.isAdmin(loginUser), BizStatus.USER_NOT_PERMISSION, "无权访问公共图库");
        } else {
            // 私有空间权限校验
            String spaceId = spaceAnalyzeDto.getSpaceId();
            ThrowUtils.throwIf(spaceId == null || "0".equals(spaceId), BizStatus.PARAMS_ERROR);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, BizStatus.NOT_FOUND_ERROR, "空间不存在");
            spaceService.checkSpacePermission(space, loginUser);
        }
    }

    /**
     * 填充查询条件
     *
     * @param spaceAnalyzeDto 空间请求参数
     * @param queryWrapper    查询条件
     */
    private void fillAnalyzeQueryWrapper(SpaceAnalyzeDTO spaceAnalyzeDto, QueryWrapper<Picture> queryWrapper) {
        if (spaceAnalyzeDto.isQueryAll()) {
            return;
        }
        if (spaceAnalyzeDto.isQueryPublic()) {
            queryWrapper.eq("space_id", "0");
            return;
        }
        String spaceId = spaceAnalyzeDto.getSpaceId();
        if (spaceId != null) {
            queryWrapper.eq("space_id", spaceId);
            return;
        }
        throw new BusinessException(BizStatus.PARAMS_ERROR, "未指定查询范围");
    }

}
