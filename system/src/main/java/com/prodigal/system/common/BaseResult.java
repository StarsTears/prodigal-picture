package com.prodigal.system.common;

import com.prodigal.system.exception.BizStatus;
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
    /** 请求追踪 ID */
    private String requestId;

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
    public BaseResult(BizStatus bizStatus) {
        this(bizStatus.getCode(), false, bizStatus.getMessage(),null);
    }
    public static <T> BaseResult<T> success() {
        return new BaseResult<T>(0, true, "成功", null);
    }

    public static <T> BaseResult<T> error() {
        BaseResult<T> baseResult = new BaseResult<T>();
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
