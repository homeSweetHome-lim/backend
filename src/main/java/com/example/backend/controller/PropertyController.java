package com.example.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.common.response.ApiResponse;
import com.example.backend.common.response.ApiResponseFactory;
import com.example.backend.dto.request.GetPropertiesByFilterRequest;
import com.example.backend.dto.request.PostPropertyRequest;
import com.example.backend.dto.response.GetPropertyInfoResponse;
import com.example.backend.service.PropertyService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/api/property")
@RestController
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    @Operation(
        summary = "데이터 요청",
        description = "공공데이터포털에 단건으로 데이터 요청")
    @GetMapping
    public ResponseEntity<ApiResponse<Void>> getProperties(
        PostPropertyRequest request
    ){
        log.info("controller 진입");
        propertyService.getProperties(request);

        return ResponseEntity.ok().body(ApiResponse.success());
    }

    @Operation(description = "비동기로 공공데이터포털에 데이터 요청 및 바로 저장 (1년치)")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> postProperties(
    ){
        propertyService.startScrapingAllRegions();

        return ResponseEntity.ok().body(ApiResponse.success());
    }

    @Operation(description = "저장된 데이터 중, 원하는 것을 찾아 반환")
    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<GetPropertyInfoResponse>>> getPropertiesByFilter(
        GetPropertiesByFilterRequest request
    ){
        return ApiResponseFactory.success(propertyService.getPropertiesByFilterResponse(request));
    }

}
