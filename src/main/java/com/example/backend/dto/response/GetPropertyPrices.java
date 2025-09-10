package com.example.backend.dto.response;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Builder
public record GetPropertyPrices(
        String maxPrice,
        String minPrice,
        Map<LocalDate, String> priceWithDateList
) {}
