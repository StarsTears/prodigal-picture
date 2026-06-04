package com.prodigal.system.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.prodigal.system.common.BaseResult;
import com.prodigal.system.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotLoginException.class)
    public BaseResult<?> notLoginExceptionHandler(Exception e) {
        log.error("NotLoginException:{}", e);
        return ResultUtils.error(ErrorCode.USER_NOT_LOGIN, e.getMessage());
    }

    @ExceptionHandler(NotPermissionException.class)
    public BaseResult<?> notPermissionExceptionHandler(Exception e) {
        log.error("NotPermissionException:{}", e);
        return ResultUtils.error(ErrorCode.USER_NOT_PERMISSION, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResult<?> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException:{}", e);
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getDefaultMessage())
                .findFirst()
                .orElse("参数校验失败");
        return ResultUtils.error(ErrorCode.PARAMS_ERROR, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public BaseResult<?> httpMessageNotReadableExceptionHandler(HttpMessageNotReadableException e) {
        log.error("HttpMessageNotReadableException:{}", e);
        return ResultUtils.error(ErrorCode.PARAMS_ERROR, "请求格式错误");
    }

    @ExceptionHandler(BusinessException.class)
    public BaseResult<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException:{}", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResult<?> RuntimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException:{}", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR);
    }
}
