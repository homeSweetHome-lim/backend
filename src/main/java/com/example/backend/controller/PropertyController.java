package com.example.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.common.response.ApiResponse;
import com.example.backend.dto.request.GetPropertyRequest;
import com.example.backend.dto.request.PublicApiRequest;
import com.example.backend.service.PropertyService;
import com.example.backend.service.PublicApiService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/api/property")
@RestController
@RequiredArgsConstructor
public class PropertyController {

    private final PublicApiService publicApiService;
    private final PropertyService propertyService;

    @Operation(
        summary = "데이터 요청",
        description = "공공데이터포털에서 데이터 요청")
    @GetMapping
    public ResponseEntity<ApiResponse<Void>> getProperties(
        GetPropertyRequest request
    ){
        log.info("controller 진입");
        propertyService.getProperties(request);

        return ResponseEntity.ok().body(ApiResponse.success());
    }

}
