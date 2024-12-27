package com.prodigal.system.model.vo;

import cn.hutool.json.JSONUtil;
import com.prodigal.system.model.entity.Space;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @create: 2024-12-19 17:38
 * @description: 空间返回参数
 **/
@Data
public class SpaceVO implements Serializable {
    private static final long serialVersionUID = -6629201306838713219L;
    /**
     * id
     */
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;

    /**
     * 空间图片的最大总大小
     */
    private Long maxSize;

    /**
     * 空间图片的最大数量
     */
    private Long maxCount;

    /**
     * 当前空间下图片的总大小
     */
    private Long totalSize;

    /**
     * 当前空间下的图片数量
     */
    private Long totalCount;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 所属用户信息
     */
    private UserVO user;

    /**
     * 封装类转对象
     * @param spaceVO 封装类
     */
    public static Space voToObj(SpaceVO spaceVO){
        if (spaceVO == null) return null;
        Space space = new Space();
        BeanUtils.copyProperties(spaceVO, space);
        return space;
    }

    /**
     * 对象转封装类
     */
    public static SpaceVO objToVO(Space space){
        if (space == null) return null;
        SpaceVO pictureVO = new SpaceVO();
        BeanUtils.copyProperties(space, pictureVO);
        return pictureVO;
    }
}