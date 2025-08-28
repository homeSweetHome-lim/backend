package com.example.backend.common.response;

import com.example.backend.common.exception.Status;
import org.springframework.http.HttpStatus;

public record ApiResponse<T>(
        int statusCode,
        String message,
        T data
) {
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(), null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(), data);
    }

    public static <T> ApiResponse<T> failure(Status status, T data) {
        return new ApiResponse<>(status.getStatusCode(), status.getMessage(), data);
    }

    public static <T> ApiResponse<T> failure(Status status) {
        return new ApiResponse<>(status.getStatusCode(), status.getMessage(), null);
    }
}
