package com.example.backend.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.entity.LawdCode;
import com.example.backend.entity.Property;

public interface PropertyRepository extends JpaRepository<Property, Long> {
    Page<Property> findByLawdCode(LawdCode lawdCode, Pageable pageable);

    boolean existsByAptName(String aptName);

    List<Property> findByAptName(String aptName);

    List<Property> findByAptNameIn(Collection<String> aptNames);
    
    /**
     * 특정 아파트 이름의 모든 Property를 조회
     * @param aptName 아파트 이름
     * @return 해당 아파트 이름의 모든 Property 리스트
     */
    List<Property> findAllByAptName(String aptName);

    List<Property> findByAptNameInAndLawdCodeLawdCodeIn(java.util.Set<java.lang.String> aptNamesInRequest, java.util.Set<java.lang.String> lawdCodesInRequest);
}
