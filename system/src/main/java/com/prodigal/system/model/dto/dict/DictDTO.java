package com.prodigal.system.model.dto.dict;

import lombok.Data;

import java.io.Serializable;

@Data
public class DictDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String dictType;

    private String dictKey;

    private String dictValue;

    private Integer sortOrder;
}
