package com.example.firstsolutions.Controller;

import com.example.firstsolutions.Aws.AWSConfig;
import com.example.firstsolutions.Services.AlertService;
import com.example.firstsolutions.Services.UniqueIdGenerator;
import com.example.firstsolutions.Services.PasswordHash;
import com.google.common.collect.ImmutableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RegisterController {

    @FXML
    public TextField firstnameTextField;
    @FXML
    public TextField lastnameTextField;
    @FXML
    public TextField usernameTextField;
    @FXML
    public TextField passwordTextField;
    private final AWSConfig awsConfig = new AWSConfig();
    private DynamoDbClient dynamoDbClient;


    @FXML
    public void onLoginButtonClick(ActionEvent actionEvent) throws IOException {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/firstsolutions/login.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 300, 400);

            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Login");

        } catch (IOException e) {
            System.err.println("Error loading login.fxml: " + e.getMessage());
        }
    }

    @FXML
    public void onRegisterButtonClick(ActionEvent actionEvent) throws IOException {
        try {
            if (dynamoDbClient == null) {
                dynamoDbClient = awsConfig.getDynamoDbClient();
            }

            String firstName = firstnameTextField.getText();
            String lastName = lastnameTextField.getText();
            String userName = usernameTextField.getText();
            String password = passwordTextField.getText();

            if (isUsernameTaken(userName)) {
                // Alert
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Registration Failed");
                alert.setHeaderText(null);
                alert.setContentText("Username is already taken. Please try again.");
                alert.showAndWait();

                // Redirect
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/firstsolutions/register.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), 300, 400);

                Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Register");
            } else {
                String uniqueUserID = UniqueIdGenerator.uniqueIdGenerator();
                String hashedPassword = PasswordHash.hashPassword(password);

                String new_Role = "Unassigned";
                Boolean readAcess = false;
                Boolean writeAcess = false;
                Boolean acessToFridge = false;

                String tableName = "Users";

                Map<String, AttributeValue> item = new HashMap<>();
                item.put("UserID", AttributeValue.builder().s(uniqueUserID).build());
                item.put("Username", AttributeValue.builder().s(userName).build());
                item.put("First Name", AttributeValue.builder().s(firstName).build());
                item.put("Last Name", AttributeValue.builder().s(lastName).build());
                item.put("Password", AttributeValue.builder().s(hashedPassword).build());
                item.put("UserRole", AttributeValue.builder().s(new_Role).build());
                item.put("DeleteAccess", AttributeValue.builder().bool(readAcess).build());
                item.put("WriteAccess", AttributeValue.builder().bool(writeAcess).build());
                item.put("AccessToFridge", AttributeValue.builder().bool(acessToFridge).build());

                PutItemRequest putItemRequest = PutItemRequest.builder()
                        .tableName(tableName)
                        .item(item)
                        .build();

                dynamoDbClient.putItem(putItemRequest);

                // Alert
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Registration Successful");
                alert.setHeaderText(null);
                alert.setContentText("You have successfully registered an account.");
                alert.showAndWait();

                // Alert Head Chef
                AlertService alertService = new AlertService();
                String alertMessage = "A new user has registered!, Please Assign a Role.";
                alertService.sendAlert("New Alert", "New Registration Alert", alertMessage);

                // Redirect
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/firstsolutions/login.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), 300, 400);

                Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Login");
            }

        } catch (DynamoDbException e) {
            // Alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("DynamoDb Error");
            alert.setHeaderText(null);
            alert.setContentText("Please Contact Tech Support");
            alert.showAndWait();

            System.err.println("Error processing registration logic: " + e.getMessage());
        } catch (IOException e) {
            // Alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("IOException Error");
            alert.setHeaderText(null);
            alert.setContentText("Please Contact Tech Support");
            alert.showAndWait();

            System.err.println("Error loading login.fxml: " + e.getMessage());
        }
    }



    private boolean isUsernameTaken(String userName) {
        String tableName = "Users";
        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(tableName)
                .filterExpression("Username = :username")
                .expressionAttributeValues(ImmutableMap.of(":username", AttributeValue.builder().s(userName).build()))
                .build();

        ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);

        return !scanResponse.items().isEmpty();
    }
}
