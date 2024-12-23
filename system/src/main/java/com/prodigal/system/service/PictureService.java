package com.prodigal.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prodigal.system.common.DeleteRequest;
import com.prodigal.system.model.dto.picture.*;
import com.prodigal.system.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
* @author Lang
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2024-12-12 15:47:24
*/
public interface PictureService extends IService<Picture> {

    PictureVO uploadPicture(Object inputSource, PictureUploadDto pictureUploadDto, User loginUser);

    int uploadPictureByBatch(PictureUploadByBatchDto pictureUploadByBatchDto, User loginUser);

    void editPicture(PictureEditDto pictureEditDto, User loginUser);

    LambdaQueryWrapper<Picture> getQueryWrapper(PictureQueryDto pictureQueryDto);

    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    void validPicture(Picture picture);

    Page<PictureVO> getPictureVOPageCache(PictureQueryDto pictureQueryDto, HttpServletRequest request);

    void doPictureReview(PictureReviewDto pictureReviewDto, User loginUser);

    void fillReviewParams(Picture picture, User loginUser);

    void deletePicture(long pictureId, User loginUser);

    void clearPictureFile(Picture oldPicture);

    void checkPicturePermission(User loginUser, Picture picture);
}
