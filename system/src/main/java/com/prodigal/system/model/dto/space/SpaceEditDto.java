package com.prodigal.system.model.dto.space;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 空间编辑请求参数
 **/
@Data
public class SpaceEditDto implements Serializable {
    private static final long serialVersionUID = -1590415227880428046L;
    /**
     * id
     */
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;
}
