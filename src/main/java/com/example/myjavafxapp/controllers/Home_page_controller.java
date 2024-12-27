package com.example.myjavafxapp.controllers;

import com.example.myjavafxapp.utils.SceneSwitcher;
import com.example.myjavafxapp.utils.ReportGenerator;
import com.example.myjavafxapp.utils.DatabaseToXMLExporter;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Главная страница (Home).
 * Содержит кнопки навигации и кнопку Create_pdf.
 */
public class Home_page_controller {

    private static final Logger logger = LoggerFactory.getLogger(Home_page_controller.class);

    @FXML public Button Create_pdf;
    @FXML private Button Member;
    @FXML private Button Judge;
    @FXML private Button Breed;
    @FXML private Button About_app;
    @FXML private Button Event;
    @FXML private Button closeButton;
    @FXML private Button hideButton;
    @FXML private Button minimizeButton;

    @FXML
    public void initialize() {
        logger.info("Инициализация Home_page_controller");

        closeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> closeApplication());
        minimizeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> minimizeApplication());
        hideButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> maximizeApplication());

        Event.setOnAction(e -> switchToScene("/com/example/myjavafxapp/fxml/page_event.fxml"));
        Member.setOnAction(e -> switchToScene("/com/example/myjavafxapp/fxml/page_applications.fxml"));
        Judge.setOnAction(e -> switchToScene("/com/example/myjavafxapp/fxml/page_judge.fxml"));
        Breed.setOnAction(e -> switchToScene("/com/example/myjavafxapp/fxml/page_breed.fxml"));
        About_app.setOnAction(e -> switchToScene("/com/example/myjavafxapp/fxml/page_aboutapp.fxml"));

        Create_pdf.setOnAction(event -> {
            logger.debug("Нажата кнопка PDF (Home_page_controller)");
            // 1) генерируем XML
            DatabaseToXMLExporter.exportAllTablesToXML("all_tables.xml");
            // 2) генерируем PDF
            new ReportGenerator().createPDF();
        });

    }

    private void switchToScene(String fxmlPath) {
        Stage currentStage = (Stage) Event.getScene().getWindow();
        SceneSwitcher.switchScene(fxmlPath, currentStage);
    }

    private void closeApplication() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    private void minimizeApplication() {
        Stage stage = (Stage) minimizeButton.getScene().getWindow();
        stage.setIconified(true);
    }

    private void maximizeApplication() {
        Stage stage = (Stage) hideButton.getScene().getWindow();
        stage.setFullScreen(!stage.isFullScreen());
    }
}
