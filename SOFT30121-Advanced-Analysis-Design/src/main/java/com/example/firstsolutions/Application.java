package com.example.firstsolutions;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.ServerSocket;

public class Application extends javafx.application.Application {

    private static final int PORT_NUMBER = 8080;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 300, 400);
        stage.setTitle("FF Smart Fridge");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        if (reservePort(PORT_NUMBER)) {
            System.out.println("Port " + PORT_NUMBER + " is reserved. Starting the application.");
            launch();
        } else {
            System.out.println("Port " + PORT_NUMBER + " could not be reserved. Another instance may be running.");
        }
    }

    private static boolean reservePort(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
