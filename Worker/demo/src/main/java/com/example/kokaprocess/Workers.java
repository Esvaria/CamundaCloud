package com.example.kokaprocess;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.kokaprocess.OrderInformation.OrderStatus;

import java.util.Map;

@SpringBootApplication
public class Workers {

    public static void main(String[] args) {
        SpringApplication.run(Workers.class, args);
    }

    @JobWorker(type = "verifyPayment")
    public void verifyPayment(final JobClient client, final ActivatedJob job) {
        try {
            // Extract variables from the job
            Map<String, Object> variables = job.getVariablesAsMap();
            // Set default order status to PENDING if not already set
            if (!variables.containsKey("orderStatus")) {
                variables.put("orderStatus", OrderStatus.PENDING.name());
            }
            String cardholderName = (String) variables.get("cardholderName");
            String cardNumber = (String) variables.get("cardNumber");
            String expiryDate = (String) variables.get("expiryDate");

            // Verify payment information
            boolean isValid = KokaProcess.getPaymentInformation(cardholderName, cardNumber, expiryDate);
            variables.put("isPaymentValid", isValid);

            if (isValid) {
                variables.put("orderStatus", OrderStatus.VALIDATED.name());
            } else {
                variables.put("orderStatus", OrderStatus.CANCELLED.name());
                System.out.println("Payment information is invalid for job: " + job.getKey());
            }

            // Complete the job with the updated variables
            client.newCompleteCommand(job.getKey())
                    .variables(variables)
                    .send()
                    .join();
        } catch (Exception e) {
            // Handle failure
            client.newFailCommand(job.getKey())
                    .retries(job.getRetries() - 1)
                    .errorMessage(e.getMessage())
                    .send()
                    .join();
        }
    }

    @JobWorker(type = "updateStock")
    public void updateStock(final JobClient client, final ActivatedJob job) {
        try {
            KokaProcess.createDatabase();

            // Extract variables from the job
            Map<String, Object> variables = job.getVariablesAsMap();
            String selectProduct = (String) variables.get("selectProduct");
            int quantityPurchased = (Integer) variables.get("quantityProduct");

            // Fetch the product's current quantity from the database
            int currentQuantity = KokaProcess.getProductQuantity(selectProduct);

            // Calculate new quantity after deduction
            int newQuantity = currentQuantity - quantityPurchased;

            // Update the product's quantity in the database
            KokaProcess.updateProductQuantity(selectProduct, newQuantity);

            // Complete the job
            client.newCompleteCommand(job.getKey()).send().join();
        } catch (Exception e) {
            // Handle failure
            client.newFailCommand(job.getKey())
                    .retries(job.getRetries() - 1)
                    .errorMessage(e.getMessage())
                    .send()
                    .join();
        }
    }

    @JobWorker(type = "generateReceipt")
    public void generateReceipt(final JobClient client, final ActivatedJob job) {
        try {
            // Extract variables from the job
            Map<String, Object> variables = job.getVariablesAsMap();

            // Extract datetime from the API response and format it
            if (variables.containsKey("datetime")) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> datetimeMap = (Map<String, Object>) variables.get("datetime");
                    if (datetimeMap != null && datetimeMap.containsKey("body")) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> body = (Map<String, Object>) datetimeMap.get("body");
                        if (body != null && body.containsKey("datetime")) {
                            String datetime = (String) body.get("datetime");
                            System.out.println("Extracted datetime: " + datetime); // Debug log
                            String formattedDateTime = KokaProcess.formatDateTime(datetime);

                            // Store formatted datetime in variables map under a new key
                            variables.put("datetimemail", formattedDateTime);
                        } else {
                            throw new IllegalArgumentException("Missing 'datetime' key in body.");
                        }
                    } else {
                        throw new IllegalArgumentException("Missing 'body' key in datetimeMap.");
                    }
                } catch (ClassCastException e) {
                    throw new IllegalArgumentException("Invalid structure for 'datetime' or 'body' in variables.", e);
                }
            } else {
                throw new IllegalArgumentException("'datetime' key not found in variables.");
            }

            // Remove datetime key from variables map
            variables.remove("datetime");

            // Create JSON file from variables
            KokaProcess.createJsonFile(variables);

            // pass order status to completed
            variables.put("orderStatus", OrderStatus.COMPLETED.name());

            // Complete the job with the updated variables
            client.newCompleteCommand(job.getKey())
                    .variables(variables)
                    .send()
                    .join();
        } catch (Exception e) {
            // Log error details for debugging
            System.err.println("Failed to handle job with key " + job.getKey() + ": " + e.getMessage());

            // Handle failure
            try {
                client.newFailCommand(job.getKey())
                        .retries(job.getRetries() - 1)
                        .errorMessage(e.getMessage())
                        .send()
                        .join();
            } catch (Exception failException) {
                System.err.println("Failed to send fail command for job with key " + job.getKey() + ": "
                        + failException.getMessage());
            }
        }
    }
}
