package com.example.backend.dto.response;

import com.example.backend.entity.enums.PropertyType;

import lombok.Builder;

@Builder
public record GetPropertyInfoResponse(
    Long propertyId,
    String state,
    String si,
    String dong,
    int maxPrice,
    int minPrice,
    Integer buildYear,
    String dealDate,
    String aptName,
    PropertyType propertyType
) {

}
