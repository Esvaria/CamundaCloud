package com.example.kokaprocess;

import com.example.kokaprocess.KokaProcess;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import java.util.Date;
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
            String cardholderName = (String) variables.get("cardholderName");
            String cardNumber = (String) variables.get("cardNumber");
            String expiryDate = (String) variables.get("expiryDate");

            // Verify payment information
            boolean isValid = KokaProcess.getPaymentInformation(cardholderName, cardNumber, expiryDate);
            variables.put("isPaymentValid", isValid);

            if (!isValid) {
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
            // Perform the task
            System.out.println("Updating stock for job: " + job.getKey());

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
            // just like verify payment we need all info to put inside the json


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
}

