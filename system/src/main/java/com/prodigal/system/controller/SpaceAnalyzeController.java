package com.prodigal.system.controller;

import com.prodigal.system.common.BaseResult;
import com.prodigal.system.common.ResultUtils;
import com.prodigal.system.exception.BizStatus;
import com.prodigal.system.exception.ThrowUtils;
import com.prodigal.system.model.dto.space.analyze.*;
import com.prodigal.system.model.entity.Space;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.model.vo.space.analyze.*;
import com.prodigal.system.service.SpaceAnalyzeService;
import com.prodigal.system.service.UserService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 空间图库分析
 **/
@RestController
@RequestMapping("/space/analyze")
public class SpaceAnalyzeController {
    @Resource
    private UserService userService;
    @Resource
    private SpaceAnalyzeService spaceAnalyzeService;

    /**
     * 空间使用情况分析
     * @param spaceUsageAnalyzeDto 分析条件
     * @param request 请求
     * @return
     */
    @PostMapping("/usage")
    public BaseResult<SpaceUsageAnalyzeVO> analyzeSpaceUsage(@RequestBody  SpaceUsageAnalyzeDTO spaceUsageAnalyzeDto, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUsageAnalyzeDto == null, BizStatus.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        SpaceUsageAnalyzeVO spaceUsageAnalyzeVO = spaceAnalyzeService.analyzeSpaceUsage(spaceUsageAnalyzeDto, loginUser);
        return ResultUtils.success(spaceUsageAnalyzeVO);
    }
    /**
     * 空间图片分类情况分析
     * @param spaceCategoryAnalyzeDto 分析条件
     * @param request 请求
     * @return
     */
    @PostMapping("/category")
    public BaseResult<List<SpaceCategoryAnalyzeVO>> analyzeSpaceCategory(@RequestBody SpaceCategoryAnalyzeDTO spaceCategoryAnalyzeDto, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceCategoryAnalyzeDto == null, BizStatus.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<SpaceCategoryAnalyzeVO> spaceCategoryAnalyzeVOList = spaceAnalyzeService.analyzeSpaceCategory(spaceCategoryAnalyzeDto, loginUser);
        return ResultUtils.success(spaceCategoryAnalyzeVOList);
    }

    /**
     * 空间图片标签情况分析
     * @param spaceTagAnalyzeDto 分析条件
     * @param request
     * @return
     */
    @PostMapping("/tag")
    public BaseResult<List<SpaceTagAnalyzeVO>> analyzeSpaceTag(@RequestBody SpaceTagAnalyzeDTO spaceTagAnalyzeDto, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceTagAnalyzeDto == null, BizStatus.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<SpaceTagAnalyzeVO> spaceTagAnalyzeVOList = spaceAnalyzeService.analyzeSpaceTag(spaceTagAnalyzeDto, loginUser);
        return ResultUtils.success(spaceTagAnalyzeVOList);
    }

    /**
     * 空间图片大小情况分析
     * @param spaceSizeAnalyzeDto 分析条件
     * @param request
     * @return
     */
    @PostMapping("/size")
    public BaseResult<List<SpaceSizeAnalyzeVO>> analyzeSpaceSize(@RequestBody SpaceSizeAnalyzeDTO spaceSizeAnalyzeDto, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceSizeAnalyzeDto == null, BizStatus.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<SpaceSizeAnalyzeVO> spaceSizeAnalyzeVOList = spaceAnalyzeService.analyzeSpaceSize(spaceSizeAnalyzeDto, loginUser);
        return ResultUtils.success(spaceSizeAnalyzeVOList);
    }

    /**
     * 用户上传行为分析
     * @param spaceUserAnalyzeDto 分析条件
     * @param request
     * @return
     */
    @PostMapping("/user")
    public BaseResult<List<SpaceUserAnalyzeVO>> analyzeSpaceUser(@RequestBody SpaceUserAnalyzeDTO spaceUserAnalyzeDto, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserAnalyzeDto == null, BizStatus.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<SpaceUserAnalyzeVO> spaceUserAnalyzeVOList = spaceAnalyzeService.analyzeSpaceUser(spaceUserAnalyzeDto, loginUser);
        return ResultUtils.success(spaceUserAnalyzeVOList);
    }

    /**
     * 空间使用排名分析
     * @param spaceRankAnalyzeDto 分析条件
     * @param request
     * @return
     */
    @PostMapping("/rank")
    public BaseResult<List<Space>> analyzeSpaceRank(@RequestBody SpaceRankAnalyzeDTO spaceRankAnalyzeDto, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceRankAnalyzeDto == null, BizStatus.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<Space> spaceList = spaceAnalyzeService.analyzeSpaceRank(spaceRankAnalyzeDto, loginUser);
        return ResultUtils.success(spaceList);
    }
}
