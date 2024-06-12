package com.example.firstsolutions.Controller;

import com.example.firstsolutions.Aws.AWSConfig;
import com.example.firstsolutions.Services.SessionManager;
import com.example.firstsolutions.Services.UniqueIdGenerator;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import static com.example.firstsolutions.Services.SessionManager.isCurrentSessionExpired;
import static com.example.firstsolutions.Services.SessionManager.logoutUser;

public class TrackController implements Initializable {

    @FXML
    public Label usernameField;

    @FXML
    public TableView<TrackItem> trackTableView;

    @FXML
    public TableColumn<TrackItem, String> TrackID;

    @FXML
    public TableColumn<TrackItem, String> Role;

    @FXML
    public TableColumn<TrackItem, String> Username;

    @FXML
    public TableColumn<TrackItem, String> ItemID;

    @FXML
    public TableColumn<TrackItem, String> Name;

    @FXML
    public TableColumn<TrackItem, String> Quantity;

    @FXML
    public TableColumn<TrackItem, String> Message;

    private static final AWSConfig awsConfig = new AWSConfig();
    private static DynamoDbClient dynamoDbClient;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (isCurrentSessionExpired()) {
            showAlertDialog("Session Expired", "Your session has expired.");
            logoutUser();
            logout();
        } else {
            String username = SessionManager.getUsername();
            if (username != null) {
                usernameField.setText(username);

                TrackID.setCellValueFactory(cellData -> cellData.getValue().getTrackIDProperty());
                Role.setCellValueFactory(cellData -> cellData.getValue().getRoleProperty());
                Username.setCellValueFactory(cellData -> cellData.getValue().getUsernameProperty());
                ItemID.setCellValueFactory(cellData -> cellData.getValue().getItemIDProperty());
                Name.setCellValueFactory(cellData -> cellData.getValue().getNameProperty());
                Quantity.setCellValueFactory(cellData -> cellData.getValue().getQuantityProperty());
                Message.setCellValueFactory(cellData -> cellData.getValue().getMessageProperty());

                List<TrackItem> allTracks = getAllTracks();

                if (allTracks.isEmpty()) {
                    showAlertDialog2("No Tracks", "There are no tracks available.", "No tracking information found at this time");
                } else {
                    trackTableView.setItems(FXCollections.observableArrayList(allTracks));
                }

            } else {
                showAlertDialog("Internal Server Problem", "System Error");
                logoutUser();
                logout();
            }
        }
    }

    private void showAlertDialog2(String title, String header, String content) {
        // Alert
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showAlertDialog(String title, String header) {
        // Alert
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

            // Alert
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

    ///ActivityTracker
    public static void trackFridgeActivity(String role, String username, String itemID, String name, String quantity, String s) {
        try {

            if (dynamoDbClient == null) {
                dynamoDbClient = AWSConfig.getDynamoDbClient();
            }

            String tableName = "Track";

            String uniqueUserID = UniqueIdGenerator.uniqueIdGenerator();

            Map<String, AttributeValue> item = new HashMap<>();
            item.put("TrackID", AttributeValue.builder().s(uniqueUserID).build());
            item.put("Role", AttributeValue.builder().s(role).build());
            item.put("Username", AttributeValue.builder().s(username).build());
            item.put("ItemID", AttributeValue.builder().s(itemID).build());
            item.put("Name", AttributeValue.builder().s(name).build());
            item.put("Quantity", AttributeValue.builder().s(quantity).build());
            item.put("Message", AttributeValue.builder().s(s).build());

            PutItemRequest putItemRequest = PutItemRequest.builder()
                    .tableName(tableName)
                    .item(item)
                    .build();

            dynamoDbClient.putItem(putItemRequest);

        } catch (DynamoDbException e) {
            // Alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("DynamoDb Error");
            alert.setHeaderText(null);
            alert.setContentText("Please Contact Tech Support");
            alert.showAndWait();

            System.err.println("Error processing tracking logic: " + e.getMessage());
        }
    }

    @FXML
    public void OnBackButtonClick(ActionEvent actionEvent) {

        try {

            if (Objects.equals(SessionManager.getRole(), "Head Chef")) {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/firstsolutions/head_chef_dashboard.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), 300, 400);

                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Head Chef Controller");
            } else if (Objects.equals(SessionManager.getRole(), "Chef")) {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/firstsolutions/chef_dashboard.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), 300, 400);

                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("-");

            } else if (Objects.equals(SessionManager.getRole(), "Delivery")) {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/firstsolutions/delivery_dashboard.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), 300, 400);

                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("-");

            } else if (Objects.equals(SessionManager.getRole(), "Server")) {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/firstsolutions/server_dashboard.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), 300, 400);

                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("-");
            }

        } catch (IOException e) {
            logoutUser();

            // Alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("FXML Error");
            alert.setHeaderText(null);
            alert.setContentText("Please Contact Tech Support");
            alert.showAndWait();

            System.err.println("Error loading dashboard: " + e.getMessage());
        }
    }


    private List<TrackItem> getAllTracks() {
        try {
            if (dynamoDbClient == null) {
                dynamoDbClient = AWSConfig.getDynamoDbClient();
            }

            String tableName = "Track";

            ScanRequest scanRequest = ScanRequest.builder().tableName(tableName).build();
            ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);

            List<TrackItem> trackItems = new ArrayList<>();

            for (Map<String, AttributeValue> item : scanResponse.items()) {
                String trackID = item.get("TrackID").s();
                String role = item.get("Role").s();
                String username = item.get("Username").s();
                String itemID = item.get("ItemID").s();
                String name = item.get("Name").s();
                String quantity = item.get("Quantity").s();
                String message = item.get("Message").s();

                trackItems.add(new TrackItem(trackID, role, username, itemID, name, quantity, message));
            }

            return trackItems;

        } catch (DynamoDbException e) {
            // Alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("DynamoDb Error");
            alert.setHeaderText(null);
            alert.setContentText("Please Contact Tech Support");
            alert.showAndWait();

            System.err.println("Error retrieving all tracking information: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public static class TrackItem {
        private final String trackID;
        private final String role;
        private final String username;
        private final String itemID;
        private final String name;
        private final String quantity;
        private final String message;

        private String status;

        public TrackItem(String trackID, String role, String username, String itemID, String name, String quantity, String message) {
            this.trackID = trackID;
            this.role = role;
            this.username = username;
            this.itemID = itemID;
            this.name = name;
            this.quantity = quantity;
            this.message = message;
            this.status = calculateStatus();
        }

        public String getStatus() {
            return status;
        }

        private String calculateStatus() {
            if ("Delivered".equalsIgnoreCase(message)) {
                return "Delivered";
            } else {
                return "In Progress";
            }
        }

        public String getTrackID() {
            return trackID;
        }

        public String getRole() {
            return role;
        }

        public String getUsername() {
            return username;
        }

        public String getItemID() {
            return itemID;
        }

        public String getName() {
            return name;
        }

        public String getQuantity() {
            return quantity;
        }

        public String getMessage() {
            return message;
        }

        public StringProperty getTrackIDProperty() {
            return new SimpleStringProperty(trackID);
        }

        public StringProperty getRoleProperty() {
            return new SimpleStringProperty(role);
        }

        public StringProperty getUsernameProperty() {
            return new SimpleStringProperty(username);
        }

        public StringProperty getItemIDProperty() {
            return new SimpleStringProperty(itemID);
        }

        public StringProperty getNameProperty() {
            return new SimpleStringProperty(name);
        }

        public StringProperty getQuantityProperty() {
            return new SimpleStringProperty(quantity);
        }

        public StringProperty getMessageProperty() {
            return new SimpleStringProperty(message);
        }

        public StringProperty getStatusProperty() {
            return new SimpleStringProperty(status);
        }
    }
}
