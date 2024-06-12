package com.example.firstsolutions.Controller.DeliveryController;

import com.example.firstsolutions.Aws.AWSConfig;
import com.example.firstsolutions.Controller.HeadChefController.HeadChef_AlertList;
import com.example.firstsolutions.Services.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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

public class Delivery_Deliveries implements Initializable {

    @FXML
    private Label usernameField;

    @FXML
    private TableView<DeliveryItem> DeliveryTableView;

    @FXML
    private TableColumn<DeliveryItem, String> DeliveryID;

    @FXML
    private TableColumn<DeliveryItem, String> ItemName;

    @FXML
    private TableColumn<DeliveryItem, String> DeliveryDate;

    @FXML
    private TableColumn<DeliveryItem, String> ItemQuantity;

    @FXML
    public MenuItem completeItemButton;

    private static final AWSConfig awsConfig = new AWSConfig();
    private static DynamoDbClient dynamoDbClient;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
            if (isCurrentSessionExpired()) {
                showAlertDialog("Session Expired", "Your session has expired.");
                logoutUser();
                logout();
            }

            else {
                String username = SessionManager.getUsername();
                if (username != null) {

                    usernameField.setText(username);

                    DeliveryID.setCellValueFactory(cellData -> cellData.getValue().getDeliveryIDProperty());
                    ItemName.setCellValueFactory(cellData -> cellData.getValue().getItemNameProperty());
                    ItemQuantity.setCellValueFactory(cellData -> cellData.getValue().getItemQuantityProperty());
                    DeliveryDate.setCellValueFactory(cellData -> cellData.getValue().getDeliveryDateProperty());

                    List<DeliveryItem> AllDeliveries = getAllDeliveries();

                    if (AllDeliveries.isEmpty()) {
                        showAlertDialog2("No Deliveries", "There are no delivery's to fulfill.", "No orders could be found at this time");
                    } else {
                        DeliveryTableView.setItems(FXCollections.observableArrayList(AllDeliveries));
                    }

                }
                else {
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
    public void OnBackButtonClick(ActionEvent actionEvent) {
        try {
            if (Objects.equals(SessionManager.getRole(), "Delivery")) {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/firstsolutions/delivery_dashboard.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), 300, 400);

                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Delivery Controller");

            }

        } catch (IOException e) {
            logoutUser();

            //Alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("FXML Error");
            alert.setHeaderText(null);
            alert.setContentText("Please Contact Tech Support");
            alert.showAndWait();

            System.err.println("Error loading dashboard: " + e.getMessage());
        }
    }

    private void reloadDataAndRefreshTable() {
        List<DeliveryItem> DeliveryItems = getAllDeliveries();
        DeliveryTableView.setItems(FXCollections.observableArrayList(DeliveryItems));
        DeliveryTableView.refresh();
    }

    List<DeliveryItem> getAllDeliveries() {

        try {
            if (dynamoDbClient == null) {
                dynamoDbClient = AWSConfig.getDynamoDbClient();
            }

            DynamoDbClient dynamoDbClient = AWSConfig.getDynamoDbClient();
            String tableName = "Delivery";

            ScanRequest scanRequest = ScanRequest.builder().tableName(tableName).build();
            ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);

            List<DeliveryItem> DeliveryItems = new ArrayList<>();

            for (Map<String, AttributeValue> item : scanResponse.items()) {
                String id = item.get("DeliveryID").s();
                String itemname = item.get("ItemName").s();
                String deliverydate = item.get("DeliveryDate").s();
                String itemquantity = item.get("Quantity").s();
                DeliveryItems.add(new DeliveryItem(id, itemname, deliverydate, itemquantity));
            }

            return DeliveryItems;

        } catch (DynamoDbException e) {
            // Alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("DynamoDb Error");
            alert.setHeaderText(null);
            alert.setContentText("Please Contact Tech Support");
            alert.showAndWait();

            System.err.println("Error retrieving all pending deliveries: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @FXML
    public void onCompleteOrderMenuItemClick(ActionEvent actionEvent) {
        DeliveryItem selectedDeliveryItem = DeliveryTableView.getSelectionModel().getSelectedItem();

        if (selectedDeliveryItem != null) {
            boolean itemsInFridge = checkItemsInFridge(selectedDeliveryItem);

            if (itemsInFridge) {
                completeDeliveryItem(selectedDeliveryItem);
            } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Incomplete Order");
                    alert.setHeaderText(null);
                    alert.setContentText("The order cannot be completed at this time. Please check the items and tracking status.");
                    alert.showAndWait();
            }

        }
    }

    private boolean checkItemsInFridge(DeliveryItem deliveryItem) {
        try {
            if (dynamoDbClient == null) {
                dynamoDbClient = AWSConfig.getDynamoDbClient();
            }

            String fridgeTableName = "Fridge";

            ScanRequest scanRequest = ScanRequest.builder()
                    .tableName(fridgeTableName)
                    .limit(1)
                    .build();

            ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);

            if (!scanResponse.items().isEmpty()) {
                String itemName = scanResponse.items().get(0).get("ItemName").s();
                String quantity = scanResponse.items().get(0).get("Quantity").s();
                System.out.println(itemName + ", " + quantity);
                return true;
            }

            return false;

        } catch (DynamoDbException e) {
            e.printStackTrace();
            return false;
        }
    }




    @FXML
    private void completeDeliveryItem(DeliveryItem item) {
    try {
        if (dynamoDbClient == null) {
            dynamoDbClient = AWSConfig.getDynamoDbClient();
        }

        String tableName = "Delivery";

        Map<String, AttributeValue> keyMap = new HashMap<>();
        keyMap.put("DeliveryID", AttributeValue.builder().s(item.getDeliveryID()).build());

        DeleteItemRequest deleteItemRequest = DeleteItemRequest.builder()
                .tableName(tableName)
                .key(keyMap)
                .build();

        dynamoDbClient.deleteItem(deleteItemRequest);

        reloadDataAndRefreshTable();

        // Alert
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Order Fulfilled");
        alert.setHeaderText(null);
        alert.setContentText("The order has been fulfilled.");
        alert.showAndWait();

    } catch (DynamoDbException e) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("An error occurred while fulfilling this order.");
        alert.showAndWait();
        }
    }

    public static class DeliveryItem {
        private final String deliveryID;
        private final String itemName;
        private final String deliveryDate;

        private final String ItemQuantity;

        public DeliveryItem(String deliveryID, String itemName, String deliveryDate, String ItemQuantity) {
            this.deliveryID = deliveryID;
            this.itemName = itemName;
            this.deliveryDate = deliveryDate;
            this.ItemQuantity = ItemQuantity;
        }

        public String getDeliveryID() {
            return deliveryID;
        }

        public String getItemName() {
            return itemName;
        }

        public String getDeliveryDate() {return deliveryDate;}

        public String getItemQuantity() {return ItemQuantity;}

        public StringProperty getDeliveryIDProperty() {
            return new SimpleStringProperty(deliveryID);
        }

        public StringProperty getItemNameProperty() {
            return new SimpleStringProperty(itemName);
        }

        public StringProperty getDeliveryDateProperty() {
            return new SimpleStringProperty(deliveryDate);
        }

        public StringProperty getItemQuantityProperty() {return new SimpleStringProperty(ItemQuantity);}
    }

}
