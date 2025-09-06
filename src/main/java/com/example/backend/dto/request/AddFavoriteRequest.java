package com.example.backend.dto.request;

import com.example.backend.entity.Property;

public record AddFavoriteRequest(
        Long propertyId
) {
}
