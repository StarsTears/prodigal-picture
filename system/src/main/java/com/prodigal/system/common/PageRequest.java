package com.prodigal.system.common;

import lombok.Data;

import jakarta.validation.constraints.Min;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 请求封装类
 **/
@Data
public class PageRequest {
    /**
     * 当前页号
     */
    @Min(value = 1, message = "页码从1开始")
    private long current = 1;
    /**
     * 页面大小
     */
    @Min(value = 1, message = "每页至少1条")
    private long pageSize=10;
    /**
     * 排序字段
     */
    private String sortField;
    /**
     * 排序顺寻(默认降序)
     */
    private String sortOrder = "descend";
}
