package com.example.kokaprocess;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

public class OrderInformation {

    @JsonProperty("selectProduct")
    private String selectProduct;
    @JsonProperty("quantityProduct")
    private float quantityProduct;

    @JsonProperty("productMap")
    private static Map<Integer, String> productMap;

    public static Map<Integer, String> getProductMap() {
        productMap = new HashMap<>();
        productMap.put(1, "selfLamp");
        productMap.put(2, "standingDesk");
        productMap.put(3, "secretLabChair");
        return productMap;
    }

    public enum OrderStatus {
        PENDING,
        COMPLETED,
        VALIDATED,
        CANCELLED
    }
}
