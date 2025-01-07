package com.prodigal.system.model.dto.spaceuser;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 编辑 空间成员 请求参数
 **/
@Data
public class SpaceUserEditDto implements Serializable {
    private static final long serialVersionUID = 5236456629496783013L;
    /**
     * id
     */
    private Long id;

    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceRole;
}
