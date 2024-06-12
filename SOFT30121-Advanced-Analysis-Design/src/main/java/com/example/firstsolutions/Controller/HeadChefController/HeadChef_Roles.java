package com.example.firstsolutions.Controller.HeadChefController;

import com.example.firstsolutions.Aws.AWSConfig;
import com.example.firstsolutions.Services.SessionManager;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
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

public class HeadChef_Roles implements Initializable {

    @FXML
    public Label usernameField;

    @FXML
    private TableView<UserRole> userTableView;

    @FXML
    private TableColumn<UserRole, String> userColumn;
    @FXML
    public TableColumn<UserRole, String> userIDColumn;

    @FXML
    private TableColumn<UserRole, String> roleColumn;
    @FXML
    public TableColumn<UserRole, Boolean> deleteColumn;
    @FXML
    public TableColumn<UserRole, Boolean> writeColumn;
    @FXML
    public TableColumn<UserRole, Boolean> accessColumn;

    private static final AWSConfig awsConfig = new AWSConfig();
    private static DynamoDbClient dynamoDbClient;

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
                userIDColumn.setCellValueFactory(cellData -> cellData.getValue().getUserIDProperty());
                userColumn.setCellValueFactory(cellData -> cellData.getValue().getUserNameProperty());
                roleColumn.setCellValueFactory(cellData -> cellData.getValue().getRoleProperty());
                deleteColumn.setCellValueFactory(cellData -> cellData.getValue().getDeleteAccessProperty());
                writeColumn.setCellValueFactory(cellData -> cellData.getValue().getWriteAccessProperty());
                accessColumn.setCellValueFactory(cellData -> cellData.getValue().getAccessToFridgeProperty());

                List<UserRole> allUserRoles = getAllUserRoles();

                userTableView.setItems(FXCollections.observableArrayList(allUserRoles));

                userTableView.setRowFactory(tableView -> {
                    final TableRow<UserRole> row = new TableRow<>();
                    final ContextMenu rowMenu = new ContextMenu();

                    MenuItem changeRoleMenuItem = new MenuItem("Change Role");
                    changeRoleMenuItem.setOnAction(event -> onChangeRoleClick(row.getItem()));
                    rowMenu.getItems().add(changeRoleMenuItem);

                    MenuItem updateDeleteMenuItem = new MenuItem("Change Delete Access");
                    updateDeleteMenuItem.setOnAction(event -> onUpdateAccessClick(row.getItem(), "DeleteAccess"));
                    rowMenu.getItems().add(updateDeleteMenuItem);

                    MenuItem updateWriteMenuItem = new MenuItem("Change Write Access");
                    updateWriteMenuItem.setOnAction(event -> onUpdateAccessClick(row.getItem(), "WriteAccess"));
                    rowMenu.getItems().add(updateWriteMenuItem);

                    MenuItem updateAccessMenuItem = new MenuItem("Change Access to Fridge");
                    updateAccessMenuItem.setOnAction(event -> onUpdateAccessClick(row.getItem(), "AccessToFridge"));
                    rowMenu.getItems().add(updateAccessMenuItem);

                    row.contextMenuProperty().bind(
                            Bindings.when(row.emptyProperty())
                                    .then((ContextMenu) null)
                                    .otherwise(rowMenu)
                    );

                    return row;
                });
            } else {
                showAlertDialog("Internal Server Problem", "System Error");
                logoutUser();
                logout();
            }

        }

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

    private void onChangeRoleClick(UserRole userRole) {
        showRoleChangeDialog(userRole);
    }

    private void showRoleChangeDialog(UserRole userRole) {
        List<String> roleOptions = getRoleOptions();

        ChoiceDialog<String> dialog = new ChoiceDialog<>(userRole.getRole(), roleOptions);
        dialog.setTitle("Change User Role");
        dialog.setHeaderText(null);
        dialog.setContentText("Select a new role:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newRole -> {
            updateUserRole(userRole.getUserID(), newRole);
        });
    }

    private List<String> getRoleOptions() {
        return List.of("Head Chef", "Chef", "Delivery", "Server", "Unassigned");
    }

    private void updateUserRole(String userID, String newRole) {
        try {
            if (dynamoDbClient == null) {
                dynamoDbClient = AWSConfig.getDynamoDbClient();
            }

            String tableName = "Users";

            Map<String, AttributeValue> attributeValues = new HashMap<>();
            attributeValues.put(":role", AttributeValue.builder().s(newRole).build());

            UpdateItemRequest updateItemRequest = UpdateItemRequest.builder()
                    .tableName(tableName)
                    .key(Collections.singletonMap("UserID", AttributeValue.builder().s(userID).build()))
                    .updateExpression("SET UserRole = :role")
                    .expressionAttributeValues(attributeValues)
                    .build();

            dynamoDbClient.updateItem(updateItemRequest);
            reloadDataAndRefreshTable();

        } catch (DynamoDbException e) {
            //Alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("DynamoDb Error");
            alert.setHeaderText(null);
            alert.setContentText("Error updating user role: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void onUpdateAccessClick(UserRole userRole, String accessType) {
        showAccessUpdateDialog(userRole, accessType);
    }

    private void showAccessUpdateDialog(UserRole userRole, String accessType) {
        Boolean currentValue = null;

        switch (accessType) {
            case "DeleteAccess":
                currentValue = userRole.isDeleteAccess();
                break;
            case "WriteAccess":
                currentValue = userRole.isWriteAccess();
                break;
            case "AccessToFridge":
                currentValue = userRole.isAccessToFridge();
                break;
        }

        if (currentValue != null) {
            ChoiceDialog<Boolean> dialog = new ChoiceDialog<>(currentValue, true, false);
            dialog.setTitle("Update Access");
            dialog.setHeaderText(null);
            dialog.setContentText("Update " + accessType + " access:");

            Optional<Boolean> result = dialog.showAndWait();
            result.ifPresent(newValue -> {
                updateUserAccess(userRole.getUserID(), accessType, newValue);
            });
        }
    }

    private void updateUserAccess(String userID, String accessType, boolean newValue) {
        try {
            if (dynamoDbClient == null) {
                dynamoDbClient = AWSConfig.getDynamoDbClient();
            }

            String tableName = "Users";

            Map<String, AttributeValue> attributeValues = new HashMap<>();
            attributeValues.put(":" + accessType, AttributeValue.builder().bool(newValue).build());

            UpdateItemRequest updateItemRequest = UpdateItemRequest.builder()
                    .tableName(tableName)
                    .key(Collections.singletonMap("UserID", AttributeValue.builder().s(userID).build()))
                    .updateExpression("SET " + accessType + " = :" + accessType)
                    .expressionAttributeValues(attributeValues)
                    .build();

            dynamoDbClient.updateItem(updateItemRequest);
            reloadDataAndRefreshTable();

        } catch (DynamoDbException e) {
            // Alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("DynamoDb Error");
            alert.setHeaderText(null);
            alert.setContentText("Error updating " + accessType + " access: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void reloadDataAndRefreshTable() {
        List<UserRole> allUserRoles = getAllUserRoles();
        userTableView.setItems(FXCollections.observableArrayList(allUserRoles));
        userTableView.refresh();
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

    public static List<UserRole> getAllUserRoles() {
        try {
            if (dynamoDbClient == null) {
                dynamoDbClient = AWSConfig.getDynamoDbClient();
            }

            DynamoDbClient dynamoDbClient = AWSConfig.getDynamoDbClient();
            String tableName = "Users";

            ScanRequest scanRequest = ScanRequest.builder().tableName(tableName).build();
            ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);

            List<UserRole> userRoles = new ArrayList<>();

            for (Map<String, AttributeValue> item : scanResponse.items()) {
                String username = item.get("Username").s();
                String userID = item.get("UserID").s();
                String role = item.get("UserRole").s();
                Boolean delete = item.get("DeleteAccess").bool();
                Boolean write = item.get("WriteAccess").bool();
                Boolean access = item.get("AccessToFridge").bool();
                userRoles.add(new UserRole(userID, username,  role, delete, write, access));
            }

            return userRoles;

        } catch (DynamoDbException e) {
            // Alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("DynamoDb Error");
            alert.setHeaderText(null);
            alert.setContentText("Please Contact Tech Support");
            alert.showAndWait();

            System.err.println("Error retrieving all user roles: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public static class UserRole {
        private final SimpleStringProperty userID;
        private final SimpleStringProperty userName;
        private final SimpleStringProperty role;
        private final SimpleBooleanProperty deleteAccess;
        private final SimpleBooleanProperty writeAccess;
        private final SimpleBooleanProperty accessToFridge;

        public UserRole(String userID, String userName, String role, boolean deleteAccess, boolean writeAccess, boolean accessToFridge) {
            this.userID = new SimpleStringProperty(userID);
            this.userName = new SimpleStringProperty(userName);
            this.role = new SimpleStringProperty(role);
            this.deleteAccess = new SimpleBooleanProperty(deleteAccess);
            this.writeAccess = new SimpleBooleanProperty(writeAccess);
            this.accessToFridge = new SimpleBooleanProperty(accessToFridge);
        }


        public SimpleStringProperty getUserIDProperty() {
            return userID;
        }

        public String getUserID() {
            return userID.get();
        }

        public void setUserID(String newUserID) {
            userID.set(newUserID);
        }

        public SimpleStringProperty getUserNameProperty() {
            return userName;
        }

        public String getUserName() {
            return userName.get();
        }

        public SimpleStringProperty getRoleProperty() {
            return role;
        }

        public String getRole() {
            return role.get();
        }

        public void setRole(String newRole) {
            role.set(newRole);
        }

        public SimpleBooleanProperty getDeleteAccessProperty() {
            return deleteAccess;
        }

        public boolean isDeleteAccess() {
            return deleteAccess.get();
        }

        public void setDeleteAccess(boolean newReadAccess) {
            deleteAccess.set(newReadAccess);
        }

        public SimpleBooleanProperty getWriteAccessProperty() {
            return writeAccess;
        }

        public boolean isWriteAccess() {
            return writeAccess.get();
        }

        public void setWriteAccess(boolean newWriteAccess) {
            writeAccess.set(newWriteAccess);
        }

        public SimpleBooleanProperty getAccessToFridgeProperty() {
            return accessToFridge;
        }

        public boolean isAccessToFridge() {
            return accessToFridge.get();
        }

        public void setAccessToFridge(boolean newAccessToFridge) {
            accessToFridge.set(newAccessToFridge);
        }
    }
}
