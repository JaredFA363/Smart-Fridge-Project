package com.example.firstsolutions.Controller.HeadChefController;

import com.example.firstsolutions.Aws.AWSConfig;
import com.example.firstsolutions.Controller.FridgeController;
import com.example.firstsolutions.Services.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import static com.example.firstsolutions.Services.SessionManager.isCurrentSessionExpired;
import static com.example.firstsolutions.Services.SessionManager.logoutUser;

public class HeadChef_AlertList implements Initializable {

    @FXML
    public Label usernameField;

    @FXML
    private TableView<AlertItem> alertsTableView;

    @FXML
    public TableColumn<AlertItem, String> alertID;

    @FXML
    private TableColumn<AlertItem, String> alertTitle;

    @FXML
    private TableColumn<AlertItem, String> alertMessage;

    private static final AWSConfig awsConfig = new AWSConfig();
    private static DynamoDbClient dynamoDbClient;

    @FXML
    public MenuItem removeItemButton;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (isCurrentSessionExpired()) {
            showAlertDialog("Session Expired", "Your session has expired.");
            logoutUser();
            logout();
        }

        else {
            String username = SessionManager.getUsername();
            if (username != null) {
                usernameField.setText(username);
                alertID.setCellValueFactory(cellData -> cellData.getValue().getAlertIDProperty());
                alertTitle.setCellValueFactory(cellData -> cellData.getValue().getTitleProperty());
                alertMessage.setCellValueFactory(cellData -> cellData.getValue().getMessageProperty());

                List<AlertItem> allAlerts = getAllAlerts();

                if (allAlerts.isEmpty()) {
                    showAlertDialog2("No Alerts", "There are no alerts.", "No alerts found at this time.");
                }
                else {
                    alertsTableView.setItems(FXCollections.observableArrayList(allAlerts));
                }

            } else {
                showAlertDialog("Internal Server Problem", "System Error");
                logoutUser();
                logout();
            }
        }
    }

    @FXML
    private void showAlertDialog2(String title, String header, String content) {
        //Alert
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    @FXML
    private void showAlertDialog(String title, String header) {
        //Alert
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText("Please log in again.");
        alert.showAndWait();
    }

    @FXML
    private void logout() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/firstsolutions/login.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 300, 400);

            //Alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("User Logged Out");
            alert.setHeaderText(null);
            alert.setContentText("You've successfully Logout.");
            alert.showAndWait();

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Login");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void OnBackButtonClick(ActionEvent actionEvent) throws IOException {
        // Redirect
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/firstsolutions/head_chef_dashboard.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 300, 400);

        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Head Chef Controller");
    }

    public static List<AlertItem> getAllAlerts(){
        try {
            if (dynamoDbClient == null) {
                dynamoDbClient = AWSConfig.getDynamoDbClient();
            }

            DynamoDbClient dynamoDbClient = AWSConfig.getDynamoDbClient();
            String tableName = "Alerts";

            ScanRequest scanRequest = ScanRequest.builder().tableName(tableName).build();
            ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);

            List<AlertItem> AlertItems = new ArrayList<>();

            for (Map<String, AttributeValue> item : scanResponse.items()) {
                String AlertID = item.get("AlertID").s();
                String title = item.get("Alert Title").s();
                String message = item.get("Alert Message").s();
                AlertItems.add(new AlertItem(AlertID, title, message));
            }

            return AlertItems;

        } catch (DynamoDbException e) {
            // Alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("DynamoDb Error");
            alert.setHeaderText(null);
            alert.setContentText("Please Contact Tech Support");
            alert.showAndWait();

            System.err.println("Error retrieving all Alerts: " + e.getMessage());
            return new ArrayList<>();
        }

    }

    @FXML
    public void onDeleteMenuItemClick(ActionEvent actionEvent) {
        HeadChef_AlertList.AlertItem selectedAlertItem = alertsTableView.getSelectionModel().getSelectedItem();

        if (selectedAlertItem != null) {{
            deleteAlertItem(selectedAlertItem);
        }
        }


    }

    private void reloadDataAndRefreshTable() {
        List<HeadChef_AlertList.AlertItem> AlertItem = getAllAlerts();
        alertsTableView.setItems(FXCollections.observableArrayList(AlertItem));
        alertsTableView.refresh();
    }
    private void deleteAlertItem(AlertItem item) {
            try {
                if (dynamoDbClient == null) {
                    dynamoDbClient = AWSConfig.getDynamoDbClient();
                }

                String tableName = "Alerts";

                Map<String, AttributeValue> keyMap = new HashMap<>();
                keyMap.put("AlertID", AttributeValue.builder().s(item.getAlertID()).build());

                DeleteItemRequest deleteItemRequest = DeleteItemRequest.builder()
                        .tableName(tableName)
                        .key(keyMap)
                        .build();

                dynamoDbClient.deleteItem(deleteItemRequest);

                reloadDataAndRefreshTable();

                // Alert
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Item Deleted");
                alert.setHeaderText(null);
                alert.setContentText("The alert has been deleted.");
                alert.showAndWait();

            } catch (DynamoDbException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("An error occurred while deleting the item from the database.");
                alert.showAndWait();
            }
    }

    public static class AlertItem {

        private final StringProperty AlertID;
        private final StringProperty title;
        private final StringProperty message;

        public AlertItem(String AlertID, String title, String message) {
            this.AlertID = new SimpleStringProperty(AlertID);
            this.title = new SimpleStringProperty(title);
            this.message = new SimpleStringProperty(message);
        }

        public StringProperty getAlertIDProperty() {
            return AlertID;
        }

        public String getAlertID() {
            return AlertID.get();
        }

        public void setAlertID(String AlertID) {
            title.set(AlertID);
        }

        public StringProperty getTitleProperty() {
            return title;
        }

        public String getTitle() {
            return title.get();
        }

        public void setTitle(String newTitle) {
            title.set(newTitle);
        }

        public StringProperty getMessageProperty() {
            return message;
        }

        public String getMessage() {
            return message.get();
        }

        public void setMessage(String newMessage) {
            message.set(newMessage);
        }
    }
}
