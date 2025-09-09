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
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@AllArgsConstructor
@Builder
@Entity
@Table(name = "properties")
@EntityListeners(AuditingEntityListener.class)
public class Property {
    
    // 기본 생성자에서 가격 필드 초기화
    public Property() {
        this.minPrice = 0;
        this.maxPrice = 0;
    }

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

    @Builder.Default
    @Column(name = "min_price")
    private Integer minPrice = 0;

    @Builder.Default
    @Column(name = "max_price")
    private Integer maxPrice = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lawd_code_id", nullable = false)
    private LawdCode lawdCode;

    /**
     * 최소/최대 가격을 업데이트하는 메서드
     * @param newMinPrice 새로운 최소 가격
     * @param newMaxPrice 새로운 최대 가격
     */
    public void updatePriceRange(Integer newMinPrice, Integer newMaxPrice) {
        if (newMinPrice == null || newMaxPrice == null) {
            return; // null 값이면 업데이트하지 않음
        }
        
        // null 값을 0으로 초기화 (안전장치)
        if (this.minPrice == null) {
            this.minPrice = 0;
        }
        if (this.maxPrice == null) {
            this.maxPrice = 0;
        }
        
        // 첫 번째 데이터인 경우 (기존 값이 0,0인 경우)
        if (this.minPrice == 0 && this.maxPrice == 0) {
            this.minPrice = newMinPrice;
            this.maxPrice = newMaxPrice;
        } else {
            // 기존 데이터와 비교하여 업데이트
            if (newMinPrice < this.minPrice || this.minPrice == 0) {
                this.minPrice = newMinPrice;
            }
            if (newMaxPrice > this.maxPrice || this.maxPrice == 0) {
                this.maxPrice = newMaxPrice;
            }
        }
    }
    
    /**
     * 단일 가격으로 최소/최대 가격을 업데이트하는 메서드 (실시간 업데이트용)
     * @param newPrice 새로운 거래 가격
     */
    public void updatePriceWithSingleValue(Integer newPrice) {
        if (newPrice == null) {
            return;
        }
        
        // null 값을 0으로 초기화 (안전장치)
        if (this.minPrice == null) {
            this.minPrice = 0;
        }
        if (this.maxPrice == null) {
            this.maxPrice = 0;
        }
        
        // 첫 번째 데이터인 경우 (기존 값이 0,0인 경우)
        if (this.minPrice == 0 && this.maxPrice == 0) {
            this.minPrice = newPrice;
            this.maxPrice = newPrice;
        } else {
            // 최소값 업데이트
            if (newPrice < this.minPrice || this.minPrice == 0) {
                this.minPrice = newPrice;
            }
            // 최대값 업데이트
            if (newPrice > this.maxPrice || this.maxPrice == 0) {
                this.maxPrice = newPrice;
            }
        }
    }
}


