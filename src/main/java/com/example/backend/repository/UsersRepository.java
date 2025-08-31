package com.example.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.entity.Users;

public interface UsersRepository extends JpaRepository<Users, Long> {
    List<Users> findUsersByEmail(String email);
}
