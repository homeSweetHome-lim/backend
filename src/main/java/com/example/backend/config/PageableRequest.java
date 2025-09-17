package com.example.backend.config;

import io.swagger.v3.oas.annotations.media.Schema;

public record PageableRequest(
    @Schema(description = "페이지 번호 (0..N)", example = "0")
    int page,

    @Schema(description = "페이지 크기", example = "10")
    int size,

    @Schema(description = "정렬(사용법: 컬럼명,ASC|DESC)", example = "aptName,asc")
    String sort
) {
}
