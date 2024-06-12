package com.example.firstsolutions.Services;

import com.example.firstsolutions.Aws.AWSConfig;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import com.example.firstsolutions.Aws.AWSConfig;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import static com.example.firstsolutions.Services.ExpiryDateService.parseExpiryDate;

public class ReportGeneratorService {

    private static final AWSConfig awsConfig = new AWSConfig();
    private static DynamoDbClient dynamoDbClient;

    private static int getTotalQuantityFridge(){
        if (dynamoDbClient == null) {
            dynamoDbClient = AWSConfig.getDynamoDbClient();
        }

        ScanRequest scanRequest = ScanRequest.builder().tableName("Fridge").build();
        ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);

        int totalQuantity = 0;
        for (Map<String, AttributeValue> item : scanResponse.items()) {
            //int quantity = Integer.parseInt(item.get("Quantity").s());
            totalQuantity += Integer.parseInt(item.get("Quantity").s());
        }
        return totalQuantity;
    }

    private static List<String> getExpiryDate3days(){
        List<String> expiryDatesWithin3Days = new ArrayList<>();

        if (dynamoDbClient == null) {
            dynamoDbClient = AWSConfig.getDynamoDbClient();
        }

        ScanRequest scanRequest = ScanRequest.builder().tableName("Fridge").build();
        ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);

        // Process the scan response
        for (Map<String, AttributeValue> item : scanResponse.items()) {
            String name = item.get("Name").s();
            if (item.containsKey("Expiry Date") && item.get("Expiry Date").s() != null) {
                String stringExpiry_Date = item.get("Expiry Date").s();
                LocalDate Expiry_Date = parseExpiryDate(item.get("Expiry Date").s());
                long daysUntilExpiry = LocalDate.now().until(Expiry_Date).getDays();
                String itemQuantity = item.get("Quantity").s();

                if (daysUntilExpiry <= 3 && daysUntilExpiry >= 0) {
                    String message = ("Expiry Date within 3 days: "+ name + " - Date: " + stringExpiry_Date + " - Quantity: " + itemQuantity);
                    expiryDatesWithin3Days.add(message);
                }
            }else {
                String message = (name + " - information not available");
                expiryDatesWithin3Days.add(message);
            }
        }
        return expiryDatesWithin3Days;
    }


    private static int getNumExpiryDate3days(){
        if (dynamoDbClient == null) {
            dynamoDbClient = AWSConfig.getDynamoDbClient();
        }

        ScanRequest scanRequest = ScanRequest.builder().tableName("Fridge").build();
        ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);

        int numOfItemsNearExpiry = 0;
        // Process the scan response
        for (Map<String, AttributeValue> item : scanResponse.items()) {
            if (item.containsKey("Expiry Date") && item.get("Expiry Date").s() != null) {
                LocalDate Expiry_Date = parseExpiryDate(item.get("Expiry Date").s());
                long daysUntilExpiry = LocalDate.now().until(Expiry_Date).getDays();

                if (daysUntilExpiry <= 3 && daysUntilExpiry >= 0) {
                    numOfItemsNearExpiry += Integer.parseInt(item.get("Quantity").s());
                }
            }
        }
        return numOfItemsNearExpiry;
    }

    private static List<String> getTrackData(){
        List<String> trackData = new ArrayList<>();

        if (dynamoDbClient == null) {
            dynamoDbClient = AWSConfig.getDynamoDbClient();
        }

        ScanRequest scanRequest = ScanRequest.builder().tableName("Track").build();
        ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);

        for (Map<String, AttributeValue> item : scanResponse.items()) {
            String message = item.get("Message").s();
            String role = item.get("Role").s();
            String quantity = item.get("Quantity").s();
            String name = item.get("Name").s();
            String username = item.get("Username").s();

            String data = ("Message: " + message + " - item: " + name + " - quantity: " + quantity + " - by: " + username + " - " + role);
            trackData.add(data);
        }

        return trackData;
    }

    private static List<String> getRecentAlerts(){
        List<String> recentAlerts = new ArrayList<>();

        if (dynamoDbClient == null) {
            dynamoDbClient = AWSConfig.getDynamoDbClient();
        }

        ScanRequest scanRequest = ScanRequest.builder().tableName("Alerts").limit(10).build();
        ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);

        for (Map<String, AttributeValue> item : scanResponse.items()) {
            if (item.containsKey("Alert Message") && item.get("Alert Message").s() != null) {
                String alertMessage = item.get("Alert Message").s();
                String data = ("Alert: " + alertMessage);
                recentAlerts.add(data);
            }
        }

        return recentAlerts;
    }

    public static boolean generateReport() {
        LocalDateTime currentDate = LocalDateTime.now();
        DateTimeFormatter formatted = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String Date = currentDate.format(formatted);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("SafetyReport_"+Date))) {
            writer.write("The following report provides an overview of the inventory status and activities related to the FFSmart Fridge:");
            writer.newLine();
            writer.newLine();

            writer.write("Inventory Overview:");
            writer.newLine();
            writer.write("Total Quantity of Items in the Fridge: " + getTotalQuantityFridge());
            writer.newLine();
            writer.write("Number of Items near Expiry: " + getNumExpiryDate3days());
            writer.newLine();
            writer.newLine();

            List<String> expiryDates = getExpiryDate3days();
            writer.write("Items Approaching Expiry:");
            writer.newLine();
            for (String expiryDate : expiryDates) {
                writer.write(expiryDate);
                writer.newLine();
            }
            writer.newLine();

            List<String> activityData = getTrackData();
            writer.write("Total Activity:");
            writer.newLine();
            for (String data : activityData){
                writer.write(data);
                writer.newLine();
            }
            writer.newLine();

            writer.write("Recent Alert History:");
            writer.newLine();
            List<String> alertData = getRecentAlerts();
            for (String alert : alertData){
                writer.write(alert);
                writer.newLine();
            }
            writer.newLine();

            writer.write("To comply with Food laws we:\n" +
                    "- Regularly check items nearing expiry and take necessary actions.\n" +
                    "- Ensure accurate inventory levels through periodic audits.\n" +
                    "- Train staff on proper handling and storage of food items.\n" +
                    "- Regularly review access controls to prevent unauthorized access.");
            writer.newLine();
            writer.newLine();

            //Getting date and time for generation date
            LocalDateTime currentDateTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedDateTime = currentDateTime.format(formatter);
            writer.write("Generated on: " + formattedDateTime);
            return true;
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
            return false;
        }
    }

}
