package com.example.firstsolutions.Controller.ChefController;

import com.example.firstsolutions.Services.AlertService;
import com.example.firstsolutions.Services.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static com.example.firstsolutions.Services.SessionManager.isCurrentSessionExpired;
import static com.example.firstsolutions.Services.SessionManager.logoutUser;

public class Chef_Controller implements Initializable {

    @FXML
    public Label usernameField;
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
