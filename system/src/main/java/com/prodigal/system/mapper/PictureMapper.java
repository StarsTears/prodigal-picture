package com.prodigal.system.mapper;

import com.prodigal.system.model.entity.Picture;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;


import java.util.Date;
import java.util.List;

/**
* @author Lang
* @description 针对表【picture(图片)】的数据库操作Mapper
* @createDate 2024-12-12 15:47:24
* @Entity com.prodigal.system.model.entity.Picture
*/
public interface PictureMapper extends BaseMapper<Picture> {

    //查询已删除且修改时间在指定时间之前的图片
    List<Picture> selectDeletedPictures (@Param("date") Date date, @Param("spaceIds") List<Long> spaceIds);

    //根据pictureIds 和 spaceIds 删除图片
    int deletePicturesByPictureIdsAndSpaceId (@Param("pictureIds") List<Long> pictureIds, @Param("spaceId") Long spaceId);

    //删除已删除且修改时间在指定时间之前的图片
    int deleteDeletedPictures (@Param("date") Date date, @Param("spaceIds") List<Long> spaceIds);

}




