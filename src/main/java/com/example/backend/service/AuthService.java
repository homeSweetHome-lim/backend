package com.example.backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.backend.common.BusinessException;
import com.example.backend.common.CommonStatus;
import com.example.backend.dto.request.LoginRequest;
import com.example.backend.dto.request.SignupRequest;
import com.example.backend.dto.response.LoginResponse;
import com.example.backend.entity.User;
import com.example.backend.entity.UserRole;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.JwtUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public void signup(SignupRequest signupRequest) {

        if (userRepository.findUserByEmail(signupRequest.email()).isPresent()) {
            throw new BusinessException(CommonStatus.ALREADY_SIGNUP_USER);
        }

        String encodedPassword = passwordEncoder.encode(signupRequest.password());

        User newUser = User.builder()
            .role(UserRole.USER)
            .nickname(signupRequest.nickname())
            .email(signupRequest.email())
            .password(encodedPassword)
            .build();

        userRepository.save(newUser);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User findUser = userRepository.findUserByEmail(request.email())
            .orElseThrow(() -> new BusinessException(CommonStatus.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.password(), findUser.getPassword())) {
            throw new BusinessException(CommonStatus.PASSWORD_NOT_MATCH);
        }

        return new LoginResponse(createAccessToken(findUser));
    }

    private String createAccessToken(User user) {
        String accessToken = jwtUtil.createToken(
            user.getEmail(),
            user.getRole(),
            user.getNickname());
        log.info("user 토큰 발급 : {}", accessToken);
        return accessToken;
    }
    // private String createRefreshToken(User user) {
    //     String refreshToken = jwtUtil.createRefreshToken(user.getId());
    //     redisTemplate.opsForValue().set(
    //         "RefreshToken:" + user.getId(),
    //         refreshToken,
    //         jwtUtil.getExpiration(refreshToken),
    //         TimeUnit.MILLISECONDS);
    //     return refreshToken;
    // }

}
