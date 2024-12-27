package com.prodigal.system.api.aliyunai.model.dto;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 图配文请求参数类
 **/
@Data
public class CreateImageSynthesisDto implements Serializable {
    private static final long serialVersionUID = 4006091922621975330L;
    /**
     * 调用的模型。wanx-ast
     */
    private String model;
    /**
     * 输入图像的基本信息，比如图像URL地址
     */
    private Input input;
    /**
     * 用于控制图片生成的参数
     */
    private Parameters parameters;

    // Inner class for 'input'
    @Data
    public static class Input {
        /**
         * 待添加的标题文本。 必填
         * 文本限制：
         * 文本数量：1个或者多个，如["标题1"]，或者["标题1"，"标题2"]。
         * 文本字数：长度没有限制。
         * 为了生图效果最佳，建议标题的数量不超过3个，每个标题的字符数不超过30
         */
        private List<String> title;
        /**
         * 待添加的副标题文本。
         * 文本限制：
         * 文本数量：1个或者多个，如["副标题1"]，或者["副标题1"，"副标题2"]。
         * 文本字数：长度没有限制。
         * 为了生图效果最佳，建议副标题的数量不超过3个，每个副标题的字符数不超过30
         */
        private List<String> subtitle;
        /**
         * 待添加的文本。
         * 文本限制：
         * 文本数量：1个或者多个，如["文本1"]，或者["文本1"，"文本2"]。
         * 文本字数：长度没有限制。
         * 为了生图效果最佳，建议文本的数量不超过3个，每个文本的字符数不超过30
         */
        private List<String> text;
        /**
         * 输入的背景图的URL地址。 必填
         * 图像限制：
         * 图像格式：目前支持PNG、JPG
         * 图像分辨率：不超过3840 x 2160像素
         * 宽高比：无限制
         * 图片大小：不超过50MB
         */
        @Alias("image_url")
        private String imageUrl;
        /**
         * 蒙版（衬底）的数量，蒙版是用来展示在文字背景的矢量元素，可以提高文字的可读性和丰富整体样式。
         * 取值范围[0, 2]，默认为0，为保证图像生成效果，建议设置为0或者1。
         */
        private Integer underlay;
        /**
         * Logo素材的URL地址。
         *
         * 图像限制：
         * 图像格式：目前支持PNG
         * 图像分辨率：不超过1280 x 1280像素
         * 图片大小：不超过5MB
         * 宽高比：无限制
         */
        private String logo;

    }

    // Inner class for 'parameters'
    @Data
    public static class Parameters {
        /**
         *期望生成的图片数量，默认为1，目前限制最多生成4张。
         */
        private Integer n;
        /**
         * temperature float 可选
         * 采样温度，用于控制模型生成图像的随机性和多样性。
         * temperature越高，生成的图像越多样性，反之，生成的图像越确定。
         * 取值范围为[0, 1.0]，默认值 0.7。
         * 由于temperature与top_p均可以控制生成图像的多样性，因此建议您只设置其中一个值。
         */
        private Double temperature;
        /**
         * 生成时，核采样方法的概率阈值，用于控制模型生成图像的多样性。
         * 例如，取值为0.8时，仅保留累计概率之和大于等于0.8的概率分布中的token，作为随机采样的候选集。
         * top_p取值越大，生成的随机性越高；取值越低，生成的随机性越低。
         * 取值范围为[0, 1.0]，默认值 0.7。
         */
        @Alias("top_p")
        private Float top_p;
    }
}
