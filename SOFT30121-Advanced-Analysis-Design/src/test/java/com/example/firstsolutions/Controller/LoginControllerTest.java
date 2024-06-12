package com.example.firstsolutions.Controller;
import com.google.common.collect.ImmutableMap;
import com.example.firstsolutions.Aws.AWSConfig;
import com.example.firstsolutions.Services.PasswordHash;
import org.junit.Before;
import org.junit.Test;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.io.IOException;

import static org.junit.Assert.*;

public class LoginControllerTest {

    private LoginController loginController;
    private DynamoDbClient dynamoDbClient;

    @Before
    public void setUp() {
        loginController = new LoginController();
        dynamoDbClient = AWSConfig.getDynamoDbClient(); // Use your actual AWS configuration for integration testing
    }

    @Test
    public void testAuthenticateUser_SuccessfulLogin() throws IOException {
        // Arrange
        String username = "test";
        String password = "test";
        String hashedPassword = PasswordHash.hashPassword(password);

        // Insert a test user into the DynamoDB table
        insertTestUser(username, hashedPassword);

        // Act
        boolean result = loginController.authenticateUser(username, password);

        // Assert
        assertTrue(result);
    }

    @Test
    public void testAuthenticateUser_FailedLogin() throws IOException {
        // Arrange
        String username = "invalidUser";
        String password = "invalidPassword";
        String hashedPassword = PasswordHash.hashPassword(password);

        // Act
        boolean result = loginController.authenticateUser(username, password);

        // Assert
        assertFalse(result);
    }

    private void insertTestUser(String username, String hashedPassword) {
        String tableName = "Users";

        ImmutableMap.Builder<String, AttributeValue> itemMapBuilder = ImmutableMap.builder();
        itemMapBuilder.put("UserID", AttributeValue.builder().s("someUserID").build()); // Adjust based on your table schema
        itemMapBuilder.put("Username", AttributeValue.builder().s(username).build());
        itemMapBuilder.put("Password", AttributeValue.builder().s(hashedPassword).build());
        itemMapBuilder.put("UserRole", AttributeValue.builder().s("ADMIN").build());
        itemMapBuilder.put("DeleteAccess", AttributeValue.builder().bool(true).build());
        itemMapBuilder.put("WriteAccess", AttributeValue.builder().bool(true).build());
        itemMapBuilder.put("AccessToFridge", AttributeValue.builder().bool(true).build());

        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(tableName)
                .item(itemMapBuilder.build())
                .build();

        dynamoDbClient.putItem(putItemRequest);
    }

}
