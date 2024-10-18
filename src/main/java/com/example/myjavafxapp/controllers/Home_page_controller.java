package com.example.myjavafxapp.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class Home_page_controller {

    @FXML
    private Button eventsButton;

    @FXML
    private Button createEventButton;

    @FXML
    private Button applicationsButton;

    @FXML
    private Button judgeButton;

    @FXML
    private Button personButton;

    @FXML
    private Button aboutAppButton;

    @FXML
    public void initialize() {
        eventsButton.setOnAction(event -> showAlert("Скоро будет"));
        createEventButton.setOnAction(event -> showAlert("Скоро будет"));
        applicationsButton.setOnAction(event -> showAlert("Скоро будет"));
        judgeButton.setOnAction(event -> showAlert("Скоро будет"));
        personButton.setOnAction(event -> showAlert("Скоро будет"));
        aboutAppButton.setOnAction(event -> showAlert("Скоро будет"));
    }

    private void showAlert(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Информация");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
