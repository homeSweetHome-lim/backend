package com.example.backend.service;

import org.springframework.stereotype.Service;

import com.example.backend.dto.request.SignupRequest;
import com.example.backend.entity.Users;
import com.example.backend.repository.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UsersRepository usersRepository;

    public String signup(SignupRequest request) {
        // 예외 추가하기
        // if (!usersRepository.findUsersByEmail(request.email()).isEmpty()){
        //     return
        // };
        Users user = Users.builder()
            .email(request.email())
            .password(request.password())
            .nickname(request.nickname())
            .build();
        usersRepository.save(user);

        return "회원가입 완료";
    }
}
