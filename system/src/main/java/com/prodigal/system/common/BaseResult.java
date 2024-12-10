package com.prodigal.system.common;

import com.prodigal.system.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 响应类基类
 **/
@Data
public class BaseResult<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private int code;
    private boolean status;
    private String msg="巭(gu)孬(nao)嫑(biao)哔哔···";
    private T data;

    public BaseResult() {
    }
    public BaseResult(Integer code, Boolean status, String msg, T data) {
        this.code = code;
        this.status = status;
        this.msg = msg;
        this.data = data;
    }
    public BaseResult(Integer code, Boolean status, T data) {
        this(code, status, "", data);
    }
    public BaseResult(ErrorCode errorCode) {
        this(errorCode.getCode(), false, errorCode.getMessage(),null);
    }
    public static BaseResult success() {
        return new BaseResult(0, true, "成功", null);
    }

    public static BaseResult error() {
        BaseResult baseResult = new BaseResult();
        return baseResult.status(false);
    }

    public  BaseResult<T> code(int code){
        this.code = code;
        return this;
    }
    public  BaseResult<T> status(boolean status){
        this.status = status;
        return this;
    }
    public  BaseResult<T> msg(String msg){
        this.msg = msg;
        return this;
    }
    public  BaseResult<T> data(T data){
        this.data = data;
        return this;
    }
}
