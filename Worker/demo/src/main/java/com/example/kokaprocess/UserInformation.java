package com.example.kokaprocess;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserInformation {

    @JsonProperty("customerName")
    private String customerName;
    @JsonProperty("phoneNumber")
    private int phoneNumber;
    @JsonProperty("customerEmail")
    private String customerEmail;

}
