package com.example.backend.controller;

import com.example.backend.common.response.ApiResponse;
import com.example.backend.common.response.ApiResponseFactory;
import com.example.backend.dto.response.UserInfoResponse;
import com.example.backend.security.AuthUser;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.backend.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @Operation(description = "유저 정보 반환")
    @GetMapping
    public ResponseEntity<ApiResponse<UserInfoResponse>> getUserInfo(
            @AuthenticationPrincipal AuthUser authUser
            ) {
        return ApiResponseFactory.success(userService.getUserInfo(authUser));
    }
}
