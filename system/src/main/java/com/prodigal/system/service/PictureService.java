package com.prodigal.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prodigal.system.api.aliyunai.model.vo.CreateOutPaintingTaskVO;
import com.prodigal.system.model.dto.picture.*;
import com.prodigal.system.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.model.vo.PictureVO;

import jakarta.servlet.http.HttpServletRequest;
import java.awt.*;
import java.util.Date;
import java.util.List;

/**
* @author Lang
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2024-12-12 15:47:24
*/
public interface PictureService extends IService<Picture> {

    PictureVO uploadPicture(Object inputSource, PictureUploadDTO pictureUploadDto, User loginUser);

    int uploadPictureByBatch(PictureUploadByBatchDTO pictureUploadByBatchDto, User loginUser);

    String getTempDownloadUrl(String id, String spaceId);

    void editPicture(PictureEditDTO pictureEditDto, User loginUser);

    void editPictureByBatch(PictureEditByBatchDTO pictureEditByBatchDto, User loginUser);

    LambdaQueryWrapper<Picture> getQueryWrapper(PictureQueryDTO pictureQueryDto);

    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    void validPicture(Picture picture);

    Page<PictureVO> getPictureVOPageCache(PictureQueryDTO pictureQueryDto, HttpServletRequest request);

    List<Picture> getPicturePageWithColor(Color targetColor, List<Picture> pictureList);

    void doPictureReview(PictureReviewDTO pictureReviewDto, User loginUser);

    void fillReviewParams(Picture picture, User loginUser, String spaceId);

    void deletePicture(String pictureId, String spaceId, User loginUser);

    void clearPictureFile(Picture oldPicture);

    List<Picture> selectDeletedPictures(Date date);

    int deletePicturesByPictureIdsAndSpaceId(List<String> pictureIds, String spaceId);

    int deleteDeletedPictures(Date date, List<String> spaceIds);

    void checkPicturePermission(User loginUser, Picture picture);

    CreateOutPaintingTaskVO createPictureOutPaintingTask(CreatePictureOutPaintingTaskDTO createPictureOutPaintingTaskDto, User loginUser);
}
