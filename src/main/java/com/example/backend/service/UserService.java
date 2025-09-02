package com.example.backend.service;

import com.example.backend.common.CommonStatus;
import com.example.backend.common.exception.BusinessException;
import com.example.backend.dto.response.UserInfoResponse;
import com.example.backend.entity.User;
import com.example.backend.security.AuthUser;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserInfoResponse getUserInfo(AuthUser authUser) {
        User findUser = userRepository.findUserByEmail(authUser.user().getEmail())
                .orElseThrow(() -> new BusinessException(CommonStatus.USER_NOT_FOUND));

        return UserInfoResponse.builder()
                .email(findUser.getEmail())
                .nickname(findUser.getNickname())
                .build();
    }
}
