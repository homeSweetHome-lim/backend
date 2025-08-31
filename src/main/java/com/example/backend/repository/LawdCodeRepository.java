package com.example.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.entity.LawdCode;

public interface LawdCodeRepository extends JpaRepository<LawdCode, Long> {

    Optional<LawdCode> findByStateAndSiAndDong(String state, String si, String dong);

    Optional<LawdCode> findByLawdCodeAndDong(String lawdCode, String umdName);
}
