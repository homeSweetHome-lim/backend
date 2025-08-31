package com.example.backend.service;

import org.springframework.stereotype.Service;

import com.example.backend.dto.request.SignupRequest;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public String signup(SignupRequest request) {
        // 예외 추가하기
        // if (!usersRepository.findUsersByEmail(request.email()).isEmpty()){
        //     return
        // };
        User user = User.builder()
            .email(request.email())
            .password(request.password())
            .nickname(request.nickname())
            .build();
        userRepository.save(user);

        return "회원가입 완료";
    }
}
