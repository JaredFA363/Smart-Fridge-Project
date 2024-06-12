package com.example.firstsolutions.Controller.DeliveryController;

import com.example.firstsolutions.Services.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

import static com.example.firstsolutions.Services.SessionManager.*;

public class Delivery_Controller implements Initializable {

    @FXML
    public Label usernameField;
    @FXML
    public Label AccessToFridgeField;

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

                if ((!hasAccessToFridge())) {
                    AccessToFridgeField.setText("Closed");
                    AccessToFridgeField.setStyle("-fx-text-fill: red; -fx-font-size: 15;");
                } else if ((hasAccessToFridge())) {
                    AccessToFridgeField.setText("Opened");
                    AccessToFridgeField.setStyle("-fx-text-fill: green; -fx-font-size: 15;");
                }

            } else {
                showAlertDialog("Internal Server Problem", "System Error");
                logoutUser();
                logout();
            }
        }
    }

    @FXML
    public void onLogoutButtonClick(ActionEvent actionEvent) {
        logoutUser();
        logout();
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

            String currentSessionId = getCurrentSessionId();
            setAccessToFridge(currentSessionId, false);

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
    public void onOpenFridgeDoorButtonClicked(ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Open Fridge Door");
        dialog.setHeaderText("Enter Passcode:");
        dialog.setContentText("Passcode:");

        String passcode = dialog.showAndWait().orElse("");

        if ("1234".equals(passcode) && (!hasAccessToFridge())) {
            String currentSessionId = getCurrentSessionId();
            setAccessToFridge(currentSessionId, true);
            AccessToFridgeField.setText("Opened");
            AccessToFridgeField.setStyle("-fx-text-fill: green; -fx-font-size: 15;");
        } else if ("1234".equals(passcode) && (hasAccessToFridge())) {
            String currentSessionId = getCurrentSessionId();
            setAccessToFridge(currentSessionId, false);
            AccessToFridgeField.setText("Closed");
            AccessToFridgeField.setStyle("-fx-text-fill: red; -fx-font-size: 15;");

        } else {

            //Alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Incorrect passcode. Fridge door remains closed.");
            alert.showAndWait();
        }

        Button openFridgeDoorButton = (Button) actionEvent.getSource();
        openFridgeDoorButton.setText(hasAccessToFridge() ? "Close Door" : "Open Door");
    }

    @FXML
    public void onDeliveryButtonClicked(ActionEvent actionEvent) throws IOException {
        if (Objects.equals(SessionManager.getRole(), "Delivery")) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/firstsolutions/Delivery_Deliveries.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 300, 400);

            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Deliveries");
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("You do not have access to the deliveries.");
            alert.showAndWait();
        }
    }

    @FXML
    public void onFridgeButtonClicked(ActionEvent actionEvent) throws IOException {
        boolean hasAccessToFridge = SessionManager.hasAccessToFridge();
        if (hasAccessToFridge) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/firstsolutions/fridge.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 300, 400);

            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Fridge");
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("You do not have access to the fridge.");
            alert.showAndWait();
        }
    }
}
