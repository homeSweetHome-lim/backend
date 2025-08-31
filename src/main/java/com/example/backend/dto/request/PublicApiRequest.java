package com.example.backend.dto.request;

import lombok.Builder;

@Builder
public record PublicApiRequest(
    String serviceKey,
    String lawdCode,    // LAWD_CD
    String dealYmd    // DEAL_YMD
) {

}
