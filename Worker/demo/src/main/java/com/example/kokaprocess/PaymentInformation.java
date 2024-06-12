package com.example.kokaprocess;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;

public class PaymentInformation {

    @JsonProperty("cardholderName")
    private String cardholderName;

    @JsonProperty("cardNumber")
    private String cardNumber;

    @JsonProperty("expiryDate")
    private String expiryDate;

    public String getCardholderName() {
        return cardholderName;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getExpiryDate() {
        return expiryDate;
    }
}
