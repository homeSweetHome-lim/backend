package com.example.backend.security;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.backend.entity.User;
import com.example.backend.entity.UserRole;

public record AuthUser(User user) implements UserDetails {
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 사용자의 권한(Role)을 GrantedAuthority 형식으로 변환합니다.
        UserRole role = user.getRole();
        String authority = role.name();

        SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(authority);
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(simpleGrantedAuthority);

        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword(); // 실제 사용자 패스워드 반환
    }

    @Override
    public String getUsername() {
        return user.getEmail(); // 사용자의 이메일을 username으로 사용
    }
}
