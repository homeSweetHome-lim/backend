package com.example.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.common.response.ApiResponse;
import com.example.backend.common.response.ApiResponseFactory;
import com.example.backend.dto.response.FindRegionNameResponse;
import com.example.backend.service.LawdCodeService;

import lombok.RequiredArgsConstructor;

@RequestMapping("/api/region")
@RestController
@RequiredArgsConstructor
public class LawdcodeController {
    private final LawdCodeService lawdCodeService;

    @GetMapping("/state/{state}")
    public ResponseEntity<ApiResponse<FindRegionNameResponse>> getSiList(
        @PathVariable String state
    ){
        return ApiResponseFactory.success(lawdCodeService.getSiNames(state));
    }

    @GetMapping("/si/{si}")
    public ResponseEntity<ApiResponse<FindRegionNameResponse>> getDongList(
        @PathVariable String si
    ){
        return ApiResponseFactory.success(lawdCodeService.getDongNames(si));
    }
}
