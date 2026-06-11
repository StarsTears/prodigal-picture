package com.prodigal.system.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.prodigal.system.common.BaseResult;
import com.prodigal.system.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotLoginException.class)
    public BaseResult<?> notLoginExceptionHandler(Exception e) {
        log.error("NotLoginException:{}", ErrorCode.USER_NOT_LOGIN.getMessage(), e);
        return ResultUtils.error(ErrorCode.USER_NOT_LOGIN, e.getMessage());
    }

    @ExceptionHandler(NotPermissionException.class)
    public BaseResult<?> notPermissionExceptionHandler(Exception e) {
        log.error("NotPermissionException:{}", ErrorCode.USER_NOT_PERMISSION.getMessage(), e);
        return ResultUtils.error(ErrorCode.USER_NOT_PERMISSION, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResult<?> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException:{}", ErrorCode.PARAMS_ERROR.getMessage(), e);
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getDefaultMessage())
                .findFirst()
                .orElse("参数校验失败");
        return ResultUtils.error(ErrorCode.PARAMS_ERROR, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public BaseResult<?> httpMessageNotReadableExceptionHandler(HttpMessageNotReadableException e) {
        log.error("HttpMessageNotReadableException:{}", e.getMessage(), e);
        String message = "请求格式错误";
        if (e.getCause() instanceof InvalidFormatException ife) {
            Class<?> targetType = ife.getTargetType();
            if (targetType.isEnum()) {
                String validValues = Arrays.stream(targetType.getEnumConstants())
                        .map(c -> ((Enum<?>) c).name())
                        .collect(Collectors.joining(", "));
                message = String.format("参数值 '%s' 不合法，合法枚举值为: [%s]", ife.getValue(), validValues);
            }
        }
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause instanceof IllegalArgumentException iae) {
                message = iae.getMessage();
                break;
            }
            cause = cause.getCause();
        }
        return ResultUtils.error(ErrorCode.PARAMS_ERROR, message);
    }

    @ExceptionHandler(BusinessException.class)
    public BaseResult<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException:{}", e.getMessage(), e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResult<?> RuntimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException:{}", e.getMessage(), e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR);
    }
}
