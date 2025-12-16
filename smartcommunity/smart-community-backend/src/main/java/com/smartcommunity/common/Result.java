package com.smartcommunity.common;

import lombok.Data;
import java.io.Serializable;

/**
 * 前后端交互的统一响应格式
 * @param <T> 数据类型
 */
@Data
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 状态码 (200=成功, 500=失败) */
    private Integer code;

    /** 返回消息 */
    private String msg;

    /** 返回数据对象 */
    private T data;

    // --- 快速构建成功结果 ---

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }

    /**
     * 新增：支持自定义消息和数据的成功返回
     */
    public static <T> Result<T> success(String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    // --- 快速构建失败结果 ---

    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMsg(msg);
        return result;
    }
}