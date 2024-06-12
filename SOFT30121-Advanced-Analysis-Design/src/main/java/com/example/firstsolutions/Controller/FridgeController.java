package com.example.firstsolutions.Controller;

import com.example.firstsolutions.Aws.AWSConfig;
import com.example.firstsolutions.Services.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import static com.example.firstsolutions.Services.SessionManager.*;

public class FridgeController implements Initializable {

    @FXML
    public Label usernameField;
    public TableView<FridgeItem> FridgeTableView;
    @FXML
    public TableColumn<FridgeItem, String> itemIDColumn;
    @FXML
    public TableColumn<FridgeItem, String> nameColumn;
    @FXML
    public TableColumn<FridgeItem, String> quantityColumn;
    @FXML
    public TableColumn<FridgeItem, String> expirydateColumn;

    @FXML
    public Button addItemButton;
    @FXML
    public MenuItem removeItemButton;
    @FXML
    public MenuItem removeQuantityButton;
    @FXML
    public MenuItem insertQuantityButton;

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

                boolean hasDeleteAccess = SessionManager.hasDeleteAccess();
                boolean hasWriteAccess = SessionManager.hasWriteAccess();

                addItemButton.setVisible(hasWriteAccess);
                removeItemButton.setVisible(hasDeleteAccess);
                removeQuantityButton.setVisible(hasDeleteAccess);
                insertQuantityButton.setVisible(hasWriteAccess);

                //alertService.receiveAndShowAlerts();

                itemIDColumn.setCellValueFactory(cellData -> cellData.getValue().getItemIDProperty());
                nameColumn.setCellValueFactory(cellData -> cellData.getValue().getNameProperty());
                quantityColumn.setCellValueFactory(cellData -> cellData.getValue().getQuantityProperty());
                expirydateColumn.setCellValueFactory(cellData -> cellData.getValue().getExpiryDateProperty());

                List<FridgeItem> allFridgeItems = getAllFridgeItems();

                if (allFridgeItems.isEmpty()) {
                    showAlertDialog2("No Items", "There are no items in the fridge.", "No items could be found in the fridge at this time");
                } else {
                    FridgeTableView.setItems(FXCollections.observableArrayList(allFridgeItems));
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
                stage.setTitle("Chef Controller");

            } else if (Objects.equals(SessionManager.getRole(), "Delivery")) {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/firstsolutions/delivery_dashboard.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), 300, 400);

                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Delivery Controller");

            }

            else if (Objects.equals(SessionManager.getRole(), "Server")) {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/firstsolutions/server_dashboard.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), 300, 400);

                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("-");

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

    List<FridgeItem> getAllFridgeItems() {

        try {
            if (dynamoDbClient == null) {
                dynamoDbClient = AWSConfig.getDynamoDbClient();
            }

            DynamoDbClient dynamoDbClient = AWSConfig.getDynamoDbClient();
            String tableName = "Fridge";

            ScanRequest scanRequest = ScanRequest.builder().tableName(tableName).build();
            ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);

            List<FridgeItem> FridgeItems = new ArrayList<>();

            for (Map<String, AttributeValue> item : scanResponse.items()) {
                String id = item.get("ItemID").s();
                String name = item.get("ItemName").s();
                String quantity = item.get("Quantity").s();
                String expiryDate = item.get("Expiry Date").s();
                FridgeItems.add(new FridgeItem(id, name, quantity, expiryDate));
            }

            return FridgeItems;

        } catch (DynamoDbException e) {
            // Alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("DynamoDb Error");
            alert.setHeaderText(null);
            alert.setContentText("Please Contact Tech Support");
            alert.showAndWait();

            System.err.println("Error retrieving all fridge items: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @FXML
    private void onAddItemClick() {
        boolean hasWriteAccess = SessionManager.hasWriteAccess();
        if (hasWriteAccess){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/firstsolutions/add_item_dialog.fxml"));
            Parent root = loader.load();

            Dialog<AddItemDialogController.FridgeItem> dialog = new Dialog<>();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Add Fridge Item");

            dialog.getDialogPane().setContent(root);

            AddItemDialogController addItemDialogController = loader.getController();
            addItemDialogController.setDialog(dialog);

            dialog.showAndWait().ifPresent(item -> addFridgeItem(item));
        } catch (IOException e) {
            e.printStackTrace();
        }
        }
        else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("You do not have insert permission.");
            alert.showAndWait();

        }
    }


    private void addFridgeItem(AddItemDialogController.FridgeItem item) {
        boolean hasWriteAccess = SessionManager.hasWriteAccess();
        if (hasWriteAccess) {
            try {

                if (dynamoDbClient == null) {
                    dynamoDbClient = AWSConfig.getDynamoDbClient();
                }

                String tableName = "Fridge";

                Map<String, AttributeValue> itemMap = new HashMap<>();
                itemMap.put("ItemID", AttributeValue.builder().s(item.getItemID()).build());
                itemMap.put("ItemName", AttributeValue.builder().s(item.getName()).build());
                itemMap.put("Quantity", AttributeValue.builder().s(item.getQuantity()).build());
                itemMap.put("Expiry Date", AttributeValue.builder().s(item.getExpiryDate()).build());

                PutItemRequest putItemRequest = PutItemRequest.builder()
                        .tableName(tableName)
                        .item(itemMap)
                        .build();

                dynamoDbClient.putItem(putItemRequest);

                TrackController.trackFridgeActivity(SessionManager.getRole(), SessionManager.getUsername(), item.getItemID(), item.getName(), item.getQuantity(), "User has added an item to the fridge");

                reloadDataAndRefreshTable();

                // Alert
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Item Added");
                alert.setHeaderText(null);
                alert.setContentText("The item has been added.");
                alert.showAndWait();

            } catch (DynamoDbException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("An error occurred while adding the item.");
                alert.showAndWait();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("You do not have insert permission.");
            alert.showAndWait();
        }

    }

    private void reloadDataAndRefreshTable() {
        List<FridgeItem> FridgeItems = getAllFridgeItems();
        FridgeTableView.setItems(FXCollections.observableArrayList(FridgeItems));
        FridgeTableView.refresh();
    }

    @FXML
    public void onInsertQuantityMenuItemClick(ActionEvent actionEvent) {
        FridgeItem selectedFridgeItem = FridgeTableView.getSelectionModel().getSelectedItem();

        if (selectedFridgeItem != null) {
            showInsertQuantityDialog(selectedFridgeItem);
        }
    }

    @FXML
    public void onDeleteMenuItemClick(ActionEvent actionEvent) {
        FridgeItem selectedFridgeItem = FridgeTableView.getSelectionModel().getSelectedItem();

        if (selectedFridgeItem != null) {{
            deleteFridgeItem(selectedFridgeItem);
        }
        }
    }

    @FXML
    public void onRemoveQuantityMenuItemClick(ActionEvent actionEvent) {
        FridgeItem selectedFridgeItem = FridgeTableView.getSelectionModel().getSelectedItem();

        if (selectedFridgeItem != null) {
            showRemoveQuantityDialog(selectedFridgeItem);
        }
    }

    private void showInsertQuantityDialog(FridgeItem selectedFridgeItem) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Quantity");
        dialog.setHeaderText("Enter the quantity to add:");
        dialog.setContentText("Quantity:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(quantity -> {
            try {
                int quantityToAdd = Integer.parseInt(quantity);
                if (quantityToAdd > 0) {
                    insertQuantityToItem(selectedFridgeItem, quantityToAdd);
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Invalid Input");
                    alert.setHeaderText(null);
                    alert.setContentText("Please input a positive integer");
                    alert.showAndWait();
                }
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Input");
                alert.setHeaderText(null);
                alert.setContentText("Please input integer value");
                alert.showAndWait();
            }
        });
    }

    private void showRemoveQuantityDialog(FridgeItem selectedFridgeItem) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Enter Quantity");
        dialog.setHeaderText("Enter the quantity you need:");
        dialog.setContentText("Quantity:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(quantity -> {
            try {
                int quantityToDelete = Integer.parseInt(quantity);
                if (quantityToDelete > 0) {
                    removeItemQuantity(selectedFridgeItem, quantityToDelete);
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Invalid Input");
                    alert.setHeaderText(null);
                    alert.setContentText("Please input a positive integer");
                    alert.showAndWait();
                }
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Input");
                alert.setHeaderText(null);
                alert.setContentText("Please input integer value");
                alert.showAndWait();
            }
        });
    }

    private void insertQuantityToItem(FridgeItem selectedFridgeItem, int quantityToAdd) {
        int currentQuantity = Integer.parseInt(selectedFridgeItem.getQuantity());
        int newQuantity = currentQuantity + quantityToAdd;

        try {
            if (dynamoDbClient == null) {
                dynamoDbClient = AWSConfig.getDynamoDbClient();
            }

            String tableName = "Fridge";

            UpdateItemRequest updateItemRequest = UpdateItemRequest.builder()
                    .tableName(tableName)
                    .key(Map.of("ItemID", AttributeValue.builder().s(selectedFridgeItem.getItemID()).build()))
                    .updateExpression("SET Quantity = :newQuantity")
                    .expressionAttributeValues(Map.of(":newQuantity", AttributeValue.builder().s(Integer.toString(newQuantity)).build()))
                    .build();

            dynamoDbClient.updateItem(updateItemRequest);

            selectedFridgeItem.setQuantity(Integer.toString(newQuantity));

            TrackController.trackFridgeActivity(SessionManager.getRole(), SessionManager.getUsername(), selectedFridgeItem.getItemID(), selectedFridgeItem.getName(), selectedFridgeItem.getQuantity(), "User has inserted a specific item quantity to the fridge");

            reloadDataAndRefreshTable();

            // Alert
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Request Filed");
            alert.setHeaderText(null);
            alert.setContentText("Quantity has been added.");
            alert.showAndWait();

        } catch (DynamoDbException e) {
            e.printStackTrace();

            //Alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("An error occurred while adding quantity.");
            alert.showAndWait();
        }
    }

    private void removeItemQuantity(FridgeItem selectedFridgeItem, int quantityToDelete) {
        int currentQuantity = Integer.parseInt(selectedFridgeItem.getQuantity());

        if (quantityToDelete > currentQuantity) {
            //Alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Quantity Request");
            alert.setHeaderText(null);
            alert.setContentText("Requested quantity exceeds available quantity in the fridge.");
            alert.showAndWait();
            return;
        }

        int newQuantity = currentQuantity - quantityToDelete;

        try {
            // DynamoDB update
            if (dynamoDbClient == null) {
                dynamoDbClient = AWSConfig.getDynamoDbClient();
            }

            String tableName = "Fridge";

            UpdateItemRequest updateItemRequest = UpdateItemRequest.builder()
                    .tableName(tableName)
                    .key(Map.of("ItemID", AttributeValue.builder().s(selectedFridgeItem.getItemID()).build()))
                    .updateExpression("SET Quantity = :newQuantity")
                    .expressionAttributeValues(Map.of(":newQuantity", AttributeValue.builder().s(Integer.toString(newQuantity)).build()))
                    .build();

            dynamoDbClient.updateItem(updateItemRequest);

            selectedFridgeItem.setQuantity(String.valueOf(newQuantity));

            TrackController.trackFridgeActivity(SessionManager.getRole(), SessionManager.getUsername(), selectedFridgeItem.getItemID(), selectedFridgeItem.getName(), selectedFridgeItem.getQuantity(), "User has taken a specific item quantity from the fridge");

            reloadDataAndRefreshTable();

            //Alert
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Request fulfilled");
            alert.setHeaderText(null);
            alert.setContentText("Your request has been fulfilled");
            alert.showAndWait();


        } catch (DynamoDbException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("An error occurred while removing quantity.");
            alert.showAndWait();
        }

    }

    private void deleteFridgeItem(FridgeItem item) {
        boolean hasDeleteAccess = SessionManager.hasDeleteAccess();
        if (hasDeleteAccess){
            try {
                if (dynamoDbClient == null) {
                    dynamoDbClient = AWSConfig.getDynamoDbClient();
                }

                String tableName = "Fridge";

                Map<String, AttributeValue> keyMap = new HashMap<>();
                keyMap.put("ItemID", AttributeValue.builder().s(item.getItemID()).build());

                DeleteItemRequest deleteItemRequest = DeleteItemRequest.builder()
                        .tableName(tableName)
                        .key(keyMap)
                        .build();

                dynamoDbClient.deleteItem(deleteItemRequest);

                TrackController.trackFridgeActivity(SessionManager.getRole(), SessionManager.getUsername(), item.getItemID(), item.getName(), item.getQuantity(), "User has delete an item from the fridge");

                reloadDataAndRefreshTable();

                // Alert
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Item Deleted");
                alert.setHeaderText(null);
                alert.setContentText("The item has been deleted.");
                alert.showAndWait();

            } catch (DynamoDbException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("An error occurred while deleting the item.");
                alert.showAndWait();
            }
        }
        else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("You do not have remove permission.");
            alert.showAndWait();
        }

    }

    public class FridgeItem {
        private final StringProperty itemID;
        private final StringProperty name;
        private final StringProperty quantity;
        private final StringProperty expiryDate;

        public FridgeItem(String itemID, String name, String quantity, String expiryDate) {
            this.itemID = new SimpleStringProperty(itemID);
            this.name = new SimpleStringProperty(name);
            this.quantity = new SimpleStringProperty(quantity);
            this.expiryDate = new SimpleStringProperty(expiryDate);
        }

        public StringProperty getItemIDProperty() {
            return itemID;
        }

        public String getItemID() {
            return itemID.get();
        }

        public void setItemID(String newID) {
            itemID.set(newID);
        }

        public StringProperty getNameProperty() {
            return name;
        }

        public String getName() {
            return name.get();
        }

        public void setName(String newName) {
            name.set(newName);
        }

        public StringProperty getQuantityProperty() {
            return quantity;
        }

        public String getQuantity() {
            return quantity.get();
        }

        public void setQuantity(String newQuantity) {
            quantity.set(newQuantity);
        }

        public StringProperty getExpiryDateProperty() {
            return expiryDate;
        }

        public String getExpiryDate() {
            return expiryDate.get();
        }

        public void setExpiryDate(String newExpiryDate) {
            expiryDate.set(newExpiryDate);
        }
    }
}
