package com.example.myjavafxapp.utils;

import exceptions.SceneSwitchException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneSwitcher {

    /**
     * Универсальный метод для переключения сцен.
     *
     * @param fxmlPath путь к FXML-файлу, который нужно загрузить.
     * @param stage текущий Stage, на котором нужно установить новую сцену.
     */
    public static void switchScene(String fxmlPath, Stage stage) {
        try {
            // Проверка наличия FXML-файла через метод
            validateFxmlPath(fxmlPath);

            // Загрузка FXML
            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource(fxmlPath));
            Parent root = loader.load();

            // Установка новой сцены
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (SceneSwitchException e) {
            e.printStackTrace();
            showAlert("Ошибка переключения сцены: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Ошибка загрузки FXML: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Произошла ошибка: " + e.getMessage());
        }
    }

    /**
     * Метод для проверки наличия FXML-файла.
     *
     * @param fxmlPath путь к FXML-файлу.
     * @throws SceneSwitchException если FXML-файл не найден.
     */
    private static void validateFxmlPath(String fxmlPath) throws SceneSwitchException {
        if (SceneSwitcher.class.getResource(fxmlPath) == null) {
            throw new SceneSwitchException("FXML файл не найден: " + fxmlPath);
        }
    }

    /**
     * Вспомогательный метод для отображения ошибок.
     *
     * @param message сообщение об ошибке.
     */
    private static void showAlert(String message) {
        // Здесь можно использовать Alert из JavaFX для вывода сообщения пользователю.
        System.err.println("ALERT: " + message);
    }
}
