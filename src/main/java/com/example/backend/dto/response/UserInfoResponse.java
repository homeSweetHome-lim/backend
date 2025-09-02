package com.example.backend.dto.response;

import lombok.Builder;

@Builder
public record UserInfoResponse(
        String email,
        String nickname
) {
}
