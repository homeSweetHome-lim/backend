package com.example.backend.common.exception;

import org.springframework.http.HttpStatus;

public interface Status {
    HttpStatus getHttpStatus();
    String getMessage();
    int getStatusCode();
}
