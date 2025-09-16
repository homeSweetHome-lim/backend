package com.example.backend.dto.response;

import java.time.LocalDate;
import java.util.List;

import com.example.backend.entity.enums.AreaType;

import lombok.Builder;

@Builder
public record GetPropertyPricesWithAreaType(
    AreaType areaType,
    List<DatePriceMap> datePriceMapList
) {
    @Builder
    public record DatePriceMap(
        LocalDate dealDate,
        String price
    ){
    }
}
