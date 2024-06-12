package com.example.firstsolutions;

import com.example.firstsolutions.Services.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

import static com.example.firstsolutions.Controller.LoginController.authenticateUser;
import static com.example.firstsolutions.Services.SessionManager.logoutUser;

public class MainController {

    @FXML
    public TextField usernametextfield;
    @FXML
    public TextField passwordtextfield;

    @FXML
    public void onLoginButtonClick(ActionEvent actionEvent) throws IOException {
        try {
            String username = usernametextfield.getText();
            String password = passwordtextfield.getText();

            boolean authentication = authenticateUser(username, password);

            if (!authentication){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Login Failed");
                alert.setHeaderText(null);
                alert.setContentText("Invalid username or password. Please try again.");
                alert.showAndWait();
            }

            if (Objects.equals(SessionManager.getRole(), "Head Chef")){
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/firstsolutions/head_chef_dashboard.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), 300, 400);
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Login Successful");
                alert.setHeaderText(null);
                alert.setContentText("You have Logged In Successfully");
                alert.showAndWait();

                Stage stage = (Stage) usernametextfield.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Head Chef Dashboard");
            }

            if (Objects.equals(SessionManager.getRole(), "Chef")){
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/firstsolutions/chef_dashboard.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), 300, 400);
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Login Successful");
                alert.setHeaderText(null);
                alert.setContentText("You have Logged In Successfully");
                alert.showAndWait();

                Stage stage = (Stage) usernametextfield.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Chef Dashboard");
            }

            if (Objects.equals(SessionManager.getRole(), "Delivery")){
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/firstsolutions/delivery_dashboard.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), 300, 400);
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Login Successful");
                alert.setHeaderText(null);
                alert.setContentText("You have Logged In Successfully");
                alert.showAndWait();

                Stage stage = (Stage) usernametextfield.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Delivery Dashboard");
            }

            if (Objects.equals(SessionManager.getRole(), "Server")){
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/firstsolutions/server_dashboard.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), 300, 400);
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Login Successful");
                alert.setHeaderText(null);
                alert.setContentText("You have Logged In Successfully");
                alert.showAndWait();

                Stage stage = (Stage) usernametextfield.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Server Dashboard");
            }

            //if no role assigned redirect
            if (Objects.equals(SessionManager.getRole(), "Unassigned")){

                logoutUser();

                // Redirect
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/firstsolutions/login.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), 300, 400);
                Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Login");

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Login Failed");
                alert.setHeaderText(null);
                alert.setContentText("Please allow 24 hours for your account to be verified");
                alert.showAndWait();


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

    @FXML
    public void onRegisterButtonClick(ActionEvent actionEvent) throws IOException {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("register.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 300, 400);

            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Register");

        } catch (IOException e) {
            //Alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("FXML Error");
            alert.setHeaderText(null);
            alert.setContentText("Please Contact Tech Support");
            alert.showAndWait();

            System.err.println("Error loading register.fxml: " + e.getMessage());
        }
    }
}
