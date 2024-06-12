package com.example.firstsolutions.Controller;

import com.example.firstsolutions.Aws.AWSConfig;
import com.example.firstsolutions.Services.ExpiryDateService;
import com.example.firstsolutions.Services.PasswordHash;
import com.example.firstsolutions.Services.SessionManager;
import com.example.firstsolutions.Services.StockCheckerService;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import com.google.common.collect.ImmutableMap;


import java.io.IOException;


public class LoginController {

    private static final AWSConfig awsConfig = new AWSConfig();
    private static DynamoDbClient dynamoDbClient;
    private static final SessionManager sessionManager = new SessionManager();

    public static boolean authenticateUser(String Username, String Password) throws IOException{

        try{

            if (dynamoDbClient == null) {
                dynamoDbClient = awsConfig.getDynamoDbClient();
            }

            String hashedPassword = PasswordHash.hashPassword(Password);

            String tableName = "Users";

            ScanRequest scanRequest = ScanRequest.builder()
                    .tableName(tableName)
                    .filterExpression("Username = :username AND Password = :password")
                    .expressionAttributeValues(ImmutableMap.of(
                            ":username", AttributeValue.builder().s(Username).build(),
                            ":password", AttributeValue.builder().s(hashedPassword).build()))

                    .build();

            ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);

            if (!scanResponse.items().isEmpty()) {
                String userRole = scanResponse.items().get(0).get("UserRole").s();
                boolean deleteAccess = scanResponse.items().get(0).get("DeleteAccess").bool();
                boolean writeAccess = scanResponse.items().get(0).get("WriteAccess").bool();
                boolean accessToFridge = scanResponse.items().get(0).get("AccessToFridge").bool();
                SessionManager.createSession(Username, userRole, deleteAccess, writeAccess, accessToFridge);
                return true;
            } else {
                return false;
            }

        }
        catch (DynamoDbException e) {
            System.err.println("Error processing login logic: " + e.getMessage());
            return false;
        }
    }

}
