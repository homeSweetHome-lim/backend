package com.example.backend.config;

public final class SecurityWhitelist {
    private SecurityWhitelist() {}

    // Swagger 및 OpenAPI 관련 공개 경로
    public static final String[] SWAGGER = new String[] {
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html"
    };

    // 인증/회원가입 등 공개 경로
    public static final String[] AUTH = new String[] {
        "/api/auth/**"
    };

    // 전체 permitAll 경로 (필요시 여기에 추가)
    public static final String[] PERMIT_ALL = new String[] {
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/api/auth/**"
    };

    // 커스텀 필터에서 아예 거르도록(미적용) 할 경로
    public static final String[] FILTER_WHITELIST = PERMIT_ALL;
}


