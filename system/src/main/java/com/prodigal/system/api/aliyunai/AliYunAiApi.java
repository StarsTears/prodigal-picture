package com.prodigal.system.api.aliyunai;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.prodigal.system.api.aliyunai.model.dto.CreateOutPaintingTaskDto;
import com.prodigal.system.api.aliyunai.model.vo.CreateOutPaintingTaskVO;
import com.prodigal.system.api.aliyunai.model.vo.GetOutPaintingTaskVO;
import com.prodigal.system.exception.BusinessException;
import com.prodigal.system.exception.ErrorCode;
import com.prodigal.system.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 阿里云AiAPi-图像扩展
 **/
@Slf4j
@Component
public class AliYunAiApi {
    //读取配置文件
    @Value("${aliYunAi.apiKey}")
    private String apiKey;
    @Value("${aliYunAi.workSpaceId}")
    private String workSpaceId;

    /**
     * 创建任务地址 POST-图像画面扩展接口
     * https://help.aliyun.com/zh/model-studio/developer-reference/image-scaling-api?spm=a2c4g.11186623.0.0.5251408dCrVyJa
     */

    private final String CREATE_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/out-painting";
    //根据任务ID查询任务 GET
    private final String GET_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/tasks/%s";

    /**
     * 创建AI扩图任务
     * @param createOutPaintingTaskDto 扩图接收参数
     * @return 扩图任务结果
     */
    public CreateOutPaintingTaskVO createOutPaintingTask(CreateOutPaintingTaskDto createOutPaintingTaskDto) {
        ThrowUtils.throwIf(createOutPaintingTaskDto == null, ErrorCode.PARAMS_ERROR,"扩图参数为空！");
        /**
         * curl --location --request POST 'https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/out-painting' \
         * --header 'X-DashScope-Async: enable' \
         * --header "Authorization: Bearer $DASHSCOPE_API_KEY" \
         * --header 'Content-Type: application/json' \
         * --data '{
         *     "model": "image-out-painting",
         *     "input": {
         *         "image_url": "https://huarong123.oss-cn-hangzhou.aliyuncs.com/image/%E5%9B%BE%E5%83%8F%E7%94%BB%E9%9D%A2%E6%89%A9%E5%B1%95.png"
         *     },
         *     "parameters": {
         *         "angle": 90,
         *         "x_scale": 1.5,
         *         "y_scale": 1.5
         *     }
         * }
         */
        HttpRequest request = HttpRequest.post(CREATE_OUT_PAINTING_TASK_URL)
                                        .header(Header.AUTHORIZATION, "Bearer " + apiKey)
                                        //必须开启异步处理 设置为enable
                                        .header("X-DashScope-Async", "enable")
                                        .header("X-DashScope-WorkSpace",workSpaceId)
                                        .header(Header.CONTENT_TYPE, ContentType.JSON.getValue())
                                        .setSSLProtocol("TLSv1.2")
                                        .body(JSONUtil.toJsonStr(createOutPaintingTaskDto));

        try(HttpResponse response = request.execute()) {
            if (!response.isOk()){
                log.error("AI 扩图失败-请求异常：{}", response.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"AI 扩图失败");
            }
            String responseBody = response.body();
            CreateOutPaintingTaskVO createOutPaintingTaskVO = JSONUtil.toBean(responseBody, CreateOutPaintingTaskVO.class);
            String errCode = createOutPaintingTaskVO.getCode();
            if (StrUtil.isNotBlank(errCode)){
                log.error("AI 扩图失败-响应异常：errCode:{} errMsg:{}",errCode, createOutPaintingTaskVO.getMessage());
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"AI 扩图接口响应异常");
            }
            return createOutPaintingTaskVO;
        }
    }

    /**
     * 根据任务ID获取扩图任务结果
     * @param taskId 任务ID
     * @return 扩图任务结果
     */

    public GetOutPaintingTaskVO getOutPaintingTask(String taskId) {
        ThrowUtils.throwIf(StrUtil.isBlank(taskId), ErrorCode.PARAMS_ERROR,"任务ID为空！");
        /**
         * curl --location --request GET 'https://dashscope.aliyuncs.com/api/v1/tasks/{task_id}' \
         * --header "Authorization: Bearer $DASHSCOPE_API_KEY"
         */
        String url = String.format(GET_OUT_PAINTING_TASK_URL, taskId);
        HttpRequest request = HttpRequest.get(url).header("Authorization", "Bearer " + apiKey)
                                                .header("X-DashScope-WorkSpace",workSpaceId)
                                                .setSSLProtocol("TLSv1.2");
        try(HttpResponse response = request.execute()){
            if (!response.isOk()){
                log.error("AI 扩图-获取任务失败：{}", response.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"AI 扩图-获取任务失败");
            }
            log.info("AI 扩图-获取任务成功：{}", JSONUtil.toJsonStr(response.body()));
            return JSONUtil.toBean(response.body(), GetOutPaintingTaskVO.class);
        }
    }
}
