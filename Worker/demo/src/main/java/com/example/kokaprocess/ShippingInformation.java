package com.example.kokaprocess;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ShippingInformation {

    @JsonProperty("streetName")
    private String streetAddress;
    @JsonProperty("streetNumber")
    private int streetNumber;
    @JsonProperty("cityNPA")
    private int NPA;
    @JsonProperty("cityName")
    private String City;
}
