package com.example.firstsolutions.Controller.HeadChefController;

import com.example.firstsolutions.Services.ExpiryDateService;
import com.example.firstsolutions.Services.SessionManager;

import static com.example.firstsolutions.Services.ReportGeneratorService.generateReport;
import static com.example.firstsolutions.Services.SessionManager.isCurrentSessionExpired;
import static com.example.firstsolutions.Services.SessionManager.logoutUser;

import com.example.firstsolutions.Services.AlertService;

import com.example.firstsolutions.Services.StockCheckerService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.control.Alert;

import java.net.URL;
import java.util.ResourceBundle;
import java.io.IOException;


public class HeadChef_Controller implements Initializable {

    @FXML
    public Label usernameField;

    private final AlertService alertService = new AlertService();


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
                StockCheckerService.checkLowStockItems();
                ExpiryDateService.checkExpiryDate3days();

                alertService.receiveAndShowAlerts();
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
    public void onAlertButtonClicked(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/firstsolutions/head_chef_alert_list.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 300, 400);

        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Alert List");
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

    @FXML
    public void onRolesButtonClick(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/firstsolutions/head_chef_roles.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 300, 400);

        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Roles");
    }

    @FXML
    public void onTrackButtonClicked(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/firstsolutions/track.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 300, 400);

        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Track");
    }

    @FXML
    public void onReportButtonClick(ActionEvent actionEvent) throws IOException {
        if (generateReport()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Report Generated");
            alert.setHeaderText(null);
            alert.setContentText("The report has been generated please check your files");
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Failed");
            alert.setHeaderText(null);
            alert.setContentText("Failed to generate report");
            alert.showAndWait();
        }
    }
}
