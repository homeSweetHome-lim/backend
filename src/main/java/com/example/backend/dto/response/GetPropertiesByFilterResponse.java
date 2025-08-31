package com.example.backend.dto.response;

import com.example.backend.entity.PropertyType;

import lombok.Builder;

@Builder
public record GetPropertiesByFilterResponse(
    String state,
    String si,
    String dong,
    String price,
    Double area,
    Integer floor,
    Integer buildYear,
    String dealDate,
    String aptName,
    PropertyType propertyType
) {
}
