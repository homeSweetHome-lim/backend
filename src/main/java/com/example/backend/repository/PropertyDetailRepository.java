package com.example.backend.repository;

import com.example.backend.entity.Property;
import com.example.backend.entity.PropertyDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropertyDetailRepository extends JpaRepository<PropertyDetail,Long> {
    List<PropertyDetail> findByProperty(Property property);
}
