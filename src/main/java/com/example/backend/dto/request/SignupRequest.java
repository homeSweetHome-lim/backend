package com.example.backend.dto.request;

public record SignupRequest(
    String email,

    String password,

    String nickname
) {
}
