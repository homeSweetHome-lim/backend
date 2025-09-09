package com.example.backend.dto.response;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record GetPropertyPrices(
        String maxPrice,
        String minPrice,
        List<priceWithDate> priceList
) {
    public static class priceWithDate{
        private final LocalDate date;
        private final String price;
        public priceWithDate(LocalDate date, String price) {
            this.date = date;
            this.price = price;
        }
    }
}
