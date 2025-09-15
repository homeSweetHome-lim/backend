package com.example.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "API 문서", version = "v1"),
        security = @SecurityRequirement(name = "JWT")
)
@SecurityScheme(
        name = "JWT",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
//@SecurityScheme(
//        name = "JWT_REFRESH", // refreshToken용
//        type = SecuritySchemeType.APIKEY,
//        in = SecuritySchemeIn.HEADER
//)
public class SwaggerConfig {
    static {
        SpringDocUtils.getConfig().replaceWithClass(Pageable.class, PageableRequest.class);
    }
}
