package com.example.backend.dto.request;

public record GetPropertyRequest(
    String state,
    String si,
    String dong,
    String apiKey
) {
}
