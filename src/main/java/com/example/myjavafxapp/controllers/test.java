package com.example.myjavafxapp.controllers;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class test extends Application {

    private List<String> data; // "База данных"

    @Override
    public void start(Stage primaryStage) {
        // Инициализация данных
        data = new ArrayList<>();
        data.add("ChatGPT");
        data.add("Character AI");
        data.add("MathHelpPlanet");
        data.add("Wattpad");
        data.add("Browser Company");
        data.add("Arc Browser");

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        Label instructions = new Label("Press Ctrl+T to open the search window.");
        root.getChildren().add(instructions);

        Scene mainScene = new Scene(root, 400, 200);

        // Добавление сочетания клавиш Ctrl+T
        KeyCombination searchShortcut = new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN);
        mainScene.getAccelerators().put(searchShortcut, this::openSearchWindow);

        primaryStage.setTitle("Search Example");
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }

    private void openSearchWindow() {
        // Окно поиска
        Stage searchStage = new Stage();
        searchStage.initModality(Modality.APPLICATION_MODAL); // Модальное окно
        searchStage.setTitle("Search");

        VBox searchRoot = new VBox(10);
        searchRoot.setPadding(new Insets(10));

        TextField searchField = new TextField();
        searchField.setPromptText("Enter search query...");

        ListView<String> resultsList = new ListView<>();

        // Добавление обработчика ввода текста
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            resultsList.getItems().clear();
            if (!newValue.isEmpty()) {
                for (String item : data) {
                    if (item.toLowerCase().contains(newValue.toLowerCase())) {
                        resultsList.getItems().add(item);
                    }
                }
            }
        });

        // Действие при выборе результата
        resultsList.setOnMouseClicked(event -> {
            String selectedItem = resultsList.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                System.out.println("Selected: " + selectedItem);
                searchStage.close(); // Закрытие окна после выбора
            }
        });

        searchRoot.getChildren().addAll(searchField, resultsList);

        Scene searchScene = new Scene(searchRoot, 300, 400);
        searchStage.setScene(searchScene);
        searchStage.showAndWait(); // Ждем закрытия окна
    }

    public static void main(String[] args) {
        launch(args);
    }
}
