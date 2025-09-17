package com.example.backend.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.backend.security.JwtAuthorizationFilter;
import com.example.backend.security.JwtUtil;
import com.example.backend.security.UserDetailsServiceImpl;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    public WebSecurityConfig(JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // CSRF 설정 비활성화
        http.csrf(AbstractHttpConfigurer::disable);
        // 세션 관리 방식을 STATELESS(상태 비저장)로 설정
        http.sessionManagement((sessionManagement) ->
            sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        // 커스텀으로 CORS 설정
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        // 예외 처리 최소 구성 (인증 실패/권한 부족 시 401/403)
        http.exceptionHandling(ex -> ex
            .authenticationEntryPoint((req, res, e) -> res.sendError(401))
            .accessDeniedHandler((req, res, e) -> res.sendError(403))
        );

        // 요청에 대한 접근 권한 설정
        http.authorizeHttpRequests((authorizeHttpRequests) ->
            authorizeHttpRequests
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll() // 정적 리소스(css, js) 허용
                .requestMatchers(SecurityWhitelist.SWAGGER).permitAll()
                .requestMatchers(SecurityWhitelist.AUTH).permitAll() // 회원가입/로그인
                .requestMatchers(SecurityWhitelist.PROPERTY).permitAll() // 매물 검색
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Preflight 요청 허용

                .anyRequest().authenticated() // 그 외 모든 요청은 인증 필요
        );

        // 우리가 만든 JwtAuthorizationFilter를 UsernamePasswordAuthenticationFilter 앞에 배치
        http.addFilterBefore(new JwtAuthorizationFilter(jwtUtil, userDetailsService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    //Cors 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 1. 허용할 URL 설정하기
        configuration.setAllowedOrigins(List.of("https://myhome.mins.work", "http://localhost:3000"));

        // 2. 허용할 HTTP 메서드
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 3. 허용할 HTTP 헤더 설정
        // 테스트용으로 일단 다 열기
        configuration.setAllowedHeaders(List.of("*"));

        // 4. Credential 여부 설정 -> 쿠키나 인증 헤더를 주고 받아야하므로 반드시 true
        configuration.setAllowCredentials(true);

        // 5. 모든 경로에 대해서 위에서 설정한 cors 설정을 적용하기
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}