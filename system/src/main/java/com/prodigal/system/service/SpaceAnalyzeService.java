package com.prodigal.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.prodigal.system.model.dto.space.analyze.*;
import com.prodigal.system.model.entity.Space;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.model.vo.space.analyze.*;

import java.util.List;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 图库空间分析
 **/
public interface SpaceAnalyzeService extends IService<Space> {
    SpaceUsageAnalyzeVO analyzeSpaceUsage(SpaceUsageAnalyzeDto spaceUsageAnalyzeDto, User loginUser);

    List<SpaceCategoryAnalyzeVO> analyzeSpaceCategory(SpaceCategoryAnalyzeDto spaceCategoryAnalyzeDto, User loginUser);

    List<SpaceTagAnalyzeVO> analyzeSpaceTag(SpaceTagAnalyzeDto spaceTagAnalyzeDto, User loginUser);

    List<SpaceSizeAnalyzeVO> analyzeSpaceSize(SpaceSizeAnalyzeDto spaceSizeAnalyzeDto, User loginUser);

    List<SpaceUserAnalyzeVO> analyzeSpaceUser(SpaceUserAnalyzeDto spaceUserAnalyzeDto, User loginUser);

    List<Space> analyzeSpaceRank(SpaceRankAnalyzeDto spaceRankAnalyzeDto, User loginUser);
}
