package com.example.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import com.example.backend.entity.enums.PropertyType;
import com.example.backend.entity.enums.TransactionType;
import jakarta.persistence.*;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "properties")
@EntityListeners(AuditingEntityListener.class)
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "property_id")
    private Long propertyId;

    @Column(name = "apt_name", length = 100, nullable = false)
    private String aptName;

    @Enumerated(EnumType.STRING)
    @Column(name = "property_type", length = 20, nullable = false)
    private PropertyType propertyType;

    @Column(name = "build_year")
    private Integer buildYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lawd_code_id", nullable = false)
    private LawdCode lawdCode;
}


