package com.prodigal.system.exception;

import lombok.Getter;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 自定义业务异常类
 **/
@Getter
public class BusinessException extends RuntimeException {
    /**
     * 错误码
     */
    private final int code;

    public BusinessException(int code, String msg) {
        super(msg);
        this.code = code;
    }
    public BusinessException(BizStatus bizStatus) {
        super(bizStatus.getMessage());
        this.code = bizStatus.getCode();
    }
    public BusinessException(BizStatus bizStatus, String msg) {
        super(msg);
        this.code = bizStatus.getCode();
    }
}
