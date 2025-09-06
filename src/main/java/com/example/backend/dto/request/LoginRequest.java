package com.example.backend.dto.request;

public record LoginRequest(
    String email,
    String password
) {
}
