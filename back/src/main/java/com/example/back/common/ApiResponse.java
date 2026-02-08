package com.example.back.common;

import lombok.Data;

/**
 * 统一返回体
 */
@Data
public class ApiResponse<T> {

    private int code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> resp = new ApiResponse<>();
        resp.code = 0;
        resp.message = "success";
        resp.data = data;
        return resp;
    }

    public static ApiResponse<Void> ok() {
        ApiResponse<Void> resp = new ApiResponse<>();
        resp.code = 0;
        resp.message = "success";
        resp.data = null;
        return resp;
    }

    public static <T> ApiResponse<T> fail(int code, String message) {
        ApiResponse<T> resp = new ApiResponse<>();
        resp.code = code;
        resp.message = message;
        return resp;
    }
}
