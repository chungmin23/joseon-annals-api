package com.spring.ai.joseonannalapi.common;

public record ApiResponse<T>(String code, String message, T data) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("SUCCESS", "ok", data);
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>("SUCCESS", "ok", null);
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
