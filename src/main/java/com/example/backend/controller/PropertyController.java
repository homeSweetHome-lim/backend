package com.example.backend.controller;

import java.util.List;

import com.example.backend.dto.response.GetPropertyDetailInfoResponse;
import com.example.backend.dto.response.GetPropertyPrices;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.common.response.ApiResponse;
import com.example.backend.common.response.ApiResponseFactory;
import com.example.backend.dto.request.PostPropertyRequest;
import com.example.backend.dto.response.GetPropertyInfoResponse;
import com.example.backend.dto.response.GetPropertyPricesWithAreaType;
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

    @Operation(description = "비동기로 데이터 풀링")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> postProperties(
    ){
        propertyService.startScrapingAllRegions();

        return ResponseEntity.ok().body(ApiResponse.success());
    }

    @Operation(description = "저장된 데이터 중, 원하는 것을 찾아 반환")
    @GetMapping("/filter/{state}/{si}/{dong}")
    public ResponseEntity<ApiResponse<Page<GetPropertyInfoResponse>>> getPropertiesByFilter(
        @PathVariable String state,
        @PathVariable String si,
        @PathVariable String dong,
        @ParameterObject Pageable pageable
    ){
        return ApiResponseFactory.success(propertyService.getContentList(state, si, dong, pageable));
    }

    @Operation(description = "property Id로 집을 찾아서 그 집의 가격+날짜 리스트와 최저&최대 가격 반환")
    @GetMapping("/price/{propertyId}")
    public ResponseEntity<ApiResponse<GetPropertyPrices>> getPropertiyPrices(
            @PathVariable Long propertyId
            ){
        return ApiResponseFactory.success(propertyService.getPropertyPrices(propertyId));
    }

    @Operation(description = "특정 아파트의 가격 범위를 업데이트합니다")
    @PostMapping("/price-range/{aptName}")
    public ResponseEntity<ApiResponse<Void>> updatePriceRange(@PathVariable String aptName) {
        propertyService.updatePriceRangeForApartment(aptName);
        return ResponseEntity.ok().body(ApiResponse.success());
    }

    @Operation(description = "모든 아파트의 가격 범위를 일괄 업데이트합니다")
    @PostMapping("/price-range/update-all")
    public ResponseEntity<ApiResponse<Void>> updateAllPriceRanges() {
        propertyService.updateAllApartmentPriceRanges();
        return ResponseEntity.ok().body(ApiResponse.success());
    }

    @GetMapping("/{propertyId}/property-list")
    public ResponseEntity<ApiResponse<List<GetPropertyDetailInfoResponse>>> getPropertyInfo(
        @PathVariable Long propertyId
    ) {
        return ApiResponseFactory.success(propertyService.getPropertyDetails(propertyId));
    }

    @GetMapping("/{propertyId}/property-list/mapping")
    public ResponseEntity<ApiResponse<List<GetPropertyPricesWithAreaType>>> getPropertyPricesDividedByArea(
        @PathVariable Long propertyId
    ) {
        return ApiResponseFactory.success(propertyService.getPropertyPricesDividedByArea(propertyId));
    }

}
