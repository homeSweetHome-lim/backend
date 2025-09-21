package com.example.backend.dto.response;

import java.util.List;

public record FindRegionNameResponse(
    String category,
    List<String> results
) {
}
