package com.example.backend.common.response;

import com.example.backend.common.exception.Status;
import org.springframework.http.ResponseEntity;

public class ApiResponseFactory {

    public static ResponseEntity<ApiResponse<Void>> success() {
        return ResponseEntity.ok(ApiResponse.success());
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(T data) {
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    public static <T> ResponseEntity<ApiResponse<Void>> failure(Status status) {
        return ResponseEntity
            .status(status.getHttpStatus())
            .body(ApiResponse.failure(status));
    }

    public static <T> ResponseEntity<ApiResponse<T>> failure(Status status, T data) {
        return ResponseEntity
            .status(status.getHttpStatus())
            .body(ApiResponse.failure(status, data));
    }
}
