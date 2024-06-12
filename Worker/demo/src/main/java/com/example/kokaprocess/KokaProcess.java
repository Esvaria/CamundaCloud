package com.example.kokaprocess;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;

@Component
public class KokaProcess {

    private static String apiKey;
    @Value("${api-ninjas.key}")
    public void setApiKey(String key) {
        apiKey = key;
    }
    public KokaProcess() {
    }

    public static boolean getPaymentInformation(String cardholderName, String cardNumber, String expiryDate) {
        // Check if the cardholder name is not null or empty
        if (cardholderName == null || cardholderName.trim().isEmpty()) {
            return false;
        }
        // Check if the card number is 16 digits long
        if (cardNumber == null || cardNumber.length() != 16) {
            return false;
        }
        // Check if the expiry date is in the future
        if (expiryDate == null || expiryDate.length() != 5) {
            return false;
        }
        return true;
    }




    private HttpResponse<String> get(String uri) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .header("X-Api-Key", apiKey)
                .header("Content-Type", "application/json")
                .uri(URI.create(uri))
                .build();

        System.out.println("Fetching URI: " + uri);
        HttpResponse<String> response = client
                .send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Got status code: " + response.statusCode());
        System.out.println("=== Body:");
        System.out.println(response.body());
        System.out.println("=========");
        return response;
    }
}
