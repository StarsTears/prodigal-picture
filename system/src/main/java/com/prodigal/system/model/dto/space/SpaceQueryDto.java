package com.prodigal.system.model.dto.space;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.prodigal.system.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 空间查询请求参数
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class SpaceQueryDto extends PageRequest implements Serializable {
    private static final long serialVersionUID = -6134565777985407409L;
    /**
     * id
     */
    private Long id;

    /**
     * 空间类型：0-私有 1-团队
     */
    private Integer spaceType;


    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;

    /**
     * 创建用户 id
     */
    private Long userId;
}
