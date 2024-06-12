package com.example.kokaprocess;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

public class OrderInformation {

    @JsonProperty("selectProduct")
    private String selectProduct;
    @JsonProperty("quantityProduct")
    private int quantityProduct;

    @JsonProperty("productMap")
    private Map<Integer, String> productMap;

    public void ProductMap() {
        productMap = new HashMap<>();
        productMap.put(1, "selfLamp");
        productMap.put(2, "standingDesk");
        productMap.put(3, "secretLabChair");
    }
    public static Map<Integer, String> getProductMap() {
        return productMap;
    }
}
