package com.prodigal.system.controller;

import com.prodigal.system.common.BaseResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @project prodigal-ai-travel
 * @author Lang
 * @since  2026/4/1
 * @Version: 1.0
 * @description 项目健康检测
 */
@RestController
@RequestMapping("health")
public class HealthController {
    @RequestMapping("/check")
    public String check() {
        return "OK";
    }

    @GetMapping("/hello")
    public BaseResult<String> hello() {
        return BaseResult.<String>success().data("hello! Prodigal Picture");
    }

}
