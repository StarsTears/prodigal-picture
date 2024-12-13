package com.prodigal.system.model.vo;

import lombok.Data;

import java.util.List;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 图片标签分类
 **/
@Data
public class PictureTagCategory {
   private List<String> tagList;
   private List<String> categoryList;
}
