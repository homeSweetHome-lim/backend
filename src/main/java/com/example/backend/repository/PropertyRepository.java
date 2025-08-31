package com.example.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.entity.LawdCode;
import com.example.backend.entity.Property;

public interface PropertyRepository extends JpaRepository<Property, Long> {
    List<Property> findByLawdCode(LawdCode lawdCode);
}
