package com.example.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.common.response.ApiResponse;
import com.example.backend.common.response.ApiResponseFactory;
import com.example.backend.dto.request.LoginRequest;
import com.example.backend.dto.request.SignupRequest;
import com.example.backend.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
public class AuthController {
    private final AuthService authService;

    @Operation(description = "회원가입 - 이메일, 비번, 닉네임")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signUp(
        @RequestBody SignupRequest signupRequest
    ){
        authService.signup(signupRequest);
        return ApiResponseFactory.success();
    }

    @Operation(description = "이메일과 비밀번호로 로그인 -> 토큰 발급")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(
        @RequestBody LoginRequest request
    ){
        return ApiResponseFactory.success(authService.login(request));
    }
}
