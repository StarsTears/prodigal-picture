package com.prodigal.system.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 空间级别封装VO类
 **/
@Data
@AllArgsConstructor
public class SpaceLevel {
    private int value;
    private String text;
    private long maxCount;
    private long maxSize;
}
