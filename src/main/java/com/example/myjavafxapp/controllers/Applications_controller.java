package com.example.myjavafxapp.controllers;

import com.example.myjavafxapp.utils.SceneSwitcher;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Контроллер для страницы Applications (возможно, устаревший).
 * Здесь управляющие кнопки для переключения на Event/Member/Judge/Breed.
 */
public class Applications_controller {

    private static final Logger logger = LoggerFactory.getLogger(Applications_controller.class);

    @FXML private Button Member;
    @FXML private Button Judge;
    @FXML private Button Breed;
    @FXML private Button About_app; // Если нужно
    @FXML private Button Event;
    @FXML private Button closeButton;
    @FXML private Button hideButton;
    @FXML private Button minimizeButton;

    @FXML
    public void initialize() {
        logger.info("Инициализация Applications_controller");

        closeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> closeApplication());
        minimizeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> minimizeApplication());
        hideButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> maximizeApplication());

        Event.setOnAction(event -> {
            logger.debug("Переключение на Event");
            switchToScene("/com/example/myjavafxapp/fxml/page_event.fxml");
        });
        Member.setOnAction(event -> {
            logger.debug("Переключение на Member");
            switchToScene("/com/example/myjavafxapp/fxml/page_applications.fxml");
        });
        Judge.setOnAction(event -> {
            logger.debug("Переключение на Judge");
            switchToScene("/com/example/myjavafxapp/fxml/page_judge.fxml");
        });
        Breed.setOnAction(event -> {
            logger.debug("Переключение на Breed");
            switchToScene("/com/example/myjavafxapp/fxml/page_breed.fxml");
        });
        // About_app.setOnAction(event -> switchToScene("/com/example/myjavafxapp/fxml/page_aboutapp.fxml"));
    }

    private void switchToScene(String fxmlPath) {
        Stage currentStage = (Stage) Event.getScene().getWindow();
        SceneSwitcher.switchScene(fxmlPath, currentStage);
    }

    private void closeApplication() {
        logger.info("Закрытие приложения (Applications_controller)");
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    private void minimizeApplication() {
        logger.info("Сворачивание приложения (Applications_controller)");
        Stage stage = (Stage) minimizeButton.getScene().getWindow();
        stage.setIconified(true);
    }

    private void maximizeApplication() {
        logger.info("Полноэкранный режим (Applications_controller)");
        Stage stage = (Stage) hideButton.getScene().getWindow();
        stage.setFullScreen(!stage.isFullScreen());
    }
}
