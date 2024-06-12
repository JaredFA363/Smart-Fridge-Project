package com.example.firstsolutions.Services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import com.example.firstsolutions.Aws.AWSConfig;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import java.util.*;

public class ExpiryDateService {

    private static final AWSConfig awsConfig = new AWSConfig();
    private static DynamoDbClient dynamoDbClient;

    public static void checkExpiryDate3days(){
        if (dynamoDbClient == null) {
            dynamoDbClient = AWSConfig.getDynamoDbClient();
        }

        ScanRequest scanRequest = ScanRequest.builder().tableName("Fridge").build();
        ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);

        // Process the scan response
        for (Map<String, AttributeValue> item : scanResponse.items()) {
            String name = item.get("ItemName").s();
            if (item.containsKey("Expiry Date") && item.get("Expiry Date").s() != null) {
                String stringExpiry_Date = item.get("Expiry Date").s();
                LocalDate Expiry_Date = parseExpiryDate(item.get("Expiry Date").s());
                long daysUntilExpiry = LocalDate.now().until(Expiry_Date).getDays();

                if (daysUntilExpiry <= 3 && daysUntilExpiry >= 0) {
                    String message = ("Expiry Date within 3 days: "+ name + " - Date: " + stringExpiry_Date);
                    // Alert HeadChef, Delivery and Chef
                    AlertService alertService = new AlertService();
                    alertService.sendAlert("New Alert", "Expiry Date Alert", message);
                }
            }else {
                String message = (name + " - Expiry information not available");
                // Alert HeadChef, Delivery and Chef
                AlertService alertService = new AlertService();
                alertService.sendAlert("New Alert", "Expiry Information Not Available", message);
            }
        }
    }

    public static LocalDate parseExpiryDate(String expiryDateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate expiryDate = LocalDate.parse(expiryDateString, formatter);
        return expiryDate;
    }
}
