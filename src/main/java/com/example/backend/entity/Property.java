package com.example.backend.entity;

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

    @Column(name = "address", length = 255, nullable = false)
    private String address;

    @Column(name = "property_type", length = 20, nullable = false)
    private PropertyType propertyType;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", length = 20, nullable = false)
    private TransactionType transactionType;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "area")
    private Double area;

    @Column(name = "floor")
    private Integer floor;

    @Column(name = "build_year")
    private Integer buildYear;

    @Column(name = "contract_date")
    private LocalDate contractDate;

    @Column(name = "lawd_cd", length = 10)
    private String lawdCode; // 지역 코드
}


