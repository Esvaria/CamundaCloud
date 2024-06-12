package com.example.kokaprocess;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ShippingInformation {

    @JsonProperty("streetAddress")
    private String streetAddress;
    @JsonProperty("streetNumber")
    private int streetNumber;
    @JsonProperty("NPA")
    private int NPA;
    @JsonProperty("City")
    private String City;
}
