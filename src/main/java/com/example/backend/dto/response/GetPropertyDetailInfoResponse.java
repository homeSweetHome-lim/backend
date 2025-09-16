package com.example.backend.dto.response;

import lombok.Builder;

@Builder
public record GetPropertyDetailInfoResponse(
    Long propertyDetailId,
    String price,
    Double area,
    String dealDate,
    Integer floor
) {
}
