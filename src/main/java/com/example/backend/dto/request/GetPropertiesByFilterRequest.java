package com.example.backend.dto.request;

public record GetPropertiesByFilterRequest(
    String state,
    String si,
    String dong
) {
}
