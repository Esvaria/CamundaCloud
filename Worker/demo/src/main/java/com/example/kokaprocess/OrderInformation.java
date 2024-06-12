package com.example.kokaprocess;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

public class OrderInformation {

    @JsonProperty("itemList")
    private List<String> itemList = Arrays.asList("selfLamp", "standingDesk", "secretLabChair");
    @JsonProperty("itemQuantity")
    private int itemQuantity;
}
