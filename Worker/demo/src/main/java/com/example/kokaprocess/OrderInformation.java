package com.example.kokaprocess;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

public class OrderInformation {

    @JsonProperty("productList")
    private List<String> productList = Arrays.asList("selfLamp", "standingDesk", "secretLabChair");
    @JsonProperty("selectProduct")
    private String selectProduct;
    @JsonProperty("quantityProduct")
    private int quantityProduct;
}
