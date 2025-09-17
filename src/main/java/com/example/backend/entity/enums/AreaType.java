package com.example.backend.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum AreaType {
    A(0, 40, 10),
    B(40, 60, 20),
    C(60, 85, 30),
    D(85, 135, 40),
    E(135, 200, 50)
    ;

    private Integer minArea;
    private Integer maxArea;
    private Integer commonArea;

    public static AreaType findByArea(double area) {
        for (AreaType areaType : values()) {
            if (area >= areaType.minArea && area < areaType.maxArea) {
                return areaType;
            }
        }
        return null;
    }
}