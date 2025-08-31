package com.example.backend.common;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final Status status;

    public BusinessException(Status status) {
        super(status.getMessage());
        this.status = status;
    }
}