package com.example.backend.entity;

import java.time.LocalDate;

import com.example.backend.entity.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "property_detail")
public class PropertyDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "price_id")
    private Long priceId;

    @Column(name = "price", nullable = false)
    private String price;

    @Column(name = "area")
    private Double area;

    @Column(name = "floor")
    private Integer floor;

    @Column(name = "deal_date")
    private LocalDate dealDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", length = 20, nullable = false)
    private TransactionType transactionType;

    @ManyToOne
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;
}
