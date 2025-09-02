package com.example.backend.entity.enums;

import lombok.Getter;

@Getter
public enum TransactionType {
    SALE, // 매매
    RENT, // 월세
    JEONSE // 전세
}
