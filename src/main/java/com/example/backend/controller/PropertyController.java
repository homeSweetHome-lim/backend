package com.example.backend.controller;

import java.util.List;

import com.example.backend.dto.response.GetPropertyPrices;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
        @RequestBody PostPropertyRequest request
    ){
        log.info("controller 진입");
        propertyService.getProperties(request);

        return ResponseEntity.ok().body(ApiResponse.success());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> postProperties(
    ){
        propertyService.startScrapingAllRegions();

        return ResponseEntity.ok().body(ApiResponse.success());
    }

    @Operation(description = "저장된 데이터 중, 원하는 것을 찾아 반환")
    @GetMapping("/filter/{state}/{si}/{dong}")
    public ResponseEntity<ApiResponse<List<GetPropertyInfoResponse>>> getPropertiesByFilter(
        @PathVariable String state,
        @PathVariable String si,
        @PathVariable String dong
    ){
        return ApiResponseFactory.success(propertyService.getPropertiesByFilterResponse(state, si, dong));
    }

    @Operation(description = "property Id로 집을 찾아서 그 집의 가격+날짜 리스트와 최저&최대 가격 반환")
    @GetMapping("/price/{propertyId}")
    public ResponseEntity<ApiResponse<GetPropertyPrices>> getPropertiyPrices(
            @PathVariable Long propertyId
            ){
        return ApiResponseFactory.success(propertyService.getPropertyPrices(propertyId));
    }

}
