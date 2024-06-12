package com.example.firstsolutions.Services;

import com.example.firstsolutions.Aws.AWSConfig;
import javafx.scene.control.Alert;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;



public class StockCheckerService {
    public StockCheckerService(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }



    private static final AWSConfig awsConfig = new AWSConfig();
    private static DynamoDbClient dynamoDbClient;

    public static boolean reOrderStock(List<String> lowStockItems) {
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        if (today == DayOfWeek.MONDAY) {
            String message = String.valueOf(lowStockItems);
            AlertService alertService = new AlertService();
            alertService.sendAlert("New Alert", "Automatic Reorder Alert", message);

            saveReorderDetails(lowStockItems);

            return true;
        }

        return false;
    }

    public static void checkLowStockItems() {
        List<String> lowStockItems = new ArrayList<>();

        if (dynamoDbClient == null) {
            dynamoDbClient = AWSConfig.getDynamoDbClient();
        }

        System.out.println("Low Stock Items:");

        ScanRequest scanRequest = ScanRequest.builder().tableName("Fridge").build();

        ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);

        for (Map<String, AttributeValue> item : scanResponse.items()) {
            String name = item.get("ItemName").s();

            if (item.containsKey("Quantity") && item.get("Quantity").s() != null) {
                int quantity = Integer.parseInt(item.get("Quantity").s());

                if (quantity < 5) {

                    String message = ("Low stock on: "+ name + " - Stock: " + quantity);
                    lowStockItems.add(name);

                    // Alert HeadChef, Delivery and Chef
                    AlertService alertService = new AlertService();
                    alertService.sendAlert("New Alert", "Low Stock Alert", message);
                }
            } else {
                String message = (name + " - Stock information not available");
                // Alert HeadChef, Delivery and Chef
                AlertService alertService = new AlertService();
                alertService.sendAlert("New Alert", "Stock Information Not Available", message);
            }
        }

        reOrderStock(lowStockItems);
    }

    private static void saveReorderDetails(List<String> lowStockItems) {
        try {
            if (dynamoDbClient == null) {
                dynamoDbClient = AWSConfig.getDynamoDbClient();
            }

            String tableName = "Delivery";
            String uniqueUserID = UniqueIdGenerator.uniqueIdGenerator();

            for (String item : lowStockItems) {
                if (!isDeliveryAlreadyRecorded(item)) {
                    Map<String, AttributeValue> deliveryItem = new HashMap<>();
                    deliveryItem.put("DeliveryID", AttributeValue.builder().s(uniqueUserID).build());
                    deliveryItem.put("ItemName", AttributeValue.builder().s(item).build());
                    deliveryItem.put("DeliveryDate", AttributeValue.builder().s(LocalDate.now().toString()).build());
                    deliveryItem.put("Quantity", AttributeValue.builder().s(String.valueOf(5)).build());

                    PutItemRequest putItemRequest = PutItemRequest.builder()
                            .tableName(tableName)
                            .item(deliveryItem)
                            .build();

                    dynamoDbClient.putItem(putItemRequest);
                }
            }

        } catch (DynamoDbException e) {
            e.printStackTrace();

            //Alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("DynamoDb Error");
            alert.setHeaderText(null);
            alert.setContentText("Error inserting delivery entry: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private static boolean isDeliveryAlreadyRecorded(String itemName) {
        try {

            if (dynamoDbClient == null) {
                dynamoDbClient = AWSConfig.getDynamoDbClient();
            }

            String tableName = "Delivery";
            String deliveryDate = LocalDate.now().toString();

            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            expressionAttributeValues.put(":itemName", AttributeValue.builder().s(itemName).build());
            expressionAttributeValues.put(":deliveryDate", AttributeValue.builder().s(deliveryDate).build());

            String filterExpression = "ItemName = :itemName AND DeliveryDate = :deliveryDate";

            ScanRequest scanRequest = ScanRequest.builder()
                    .tableName(tableName)
                    .filterExpression(filterExpression)
                    .expressionAttributeValues(expressionAttributeValues)
                    .build();

            ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);

            return !scanResponse.items().isEmpty();

        } catch (DynamoDbException e) {
            e.printStackTrace();

            //Alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("DynamoDb Error");
            alert.setHeaderText(null);
            alert.setContentText("Error checking delivery entries: " + e.getMessage());
            alert.showAndWait();
            return false;
        }
    }
}
