package com.example.myjavafxapp.controllers;

import com.example.myjavafxapp.utils.ReportGenerator;
import com.example.myjavafxapp.utils.SceneSwitcher;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Контроллер для страницы "About app", отображающей инструкцию пользования приложением.
 * <p>
 * Содержит кнопки управления окном (закрыть, свернуть, полноэкранный режим),
 * а также кнопки навигации (Event, Member, Judge, Breed, Create_pdf).
 * Кнопка <strong>backToHome</strong> возвращает на домашнюю страницу.
 * </p>
 */
public class AboutApp_controller {

    private static final Logger logger = LoggerFactory.getLogger(AboutApp_controller.class);

    // Кнопки управления окном
    @FXML private Button closeButton;
    @FXML private Button hideButton;
    @FXML private Button minimizeButton;

    // Кнопки навигации (как и в других контроллерах)
    @FXML private Button Event;
    @FXML private Button Member;
    @FXML private Button Judge;
    @FXML private Button Breed;
    @FXML private Button Create_pdf;

    // Кнопка "Назад на главную"
    @FXML private Button backToHome;

    // Прокручиваемая область и текст с инструкцией
    @FXML private ScrollPane aboutScrollPane;
    @FXML private TextArea aboutText;

    /**
     * Инициализация контроллера AboutApp_controller.
     * Настройка кнопок управления окном, навигации, а также вывод инструкции.
     */
    @FXML
    public void initialize() {
        logger.info("Инициализация AboutApp_controller");

        // Кнопки управления окном
        closeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            logger.debug("Закрытие приложения из AboutApp");
            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.close();
        });
        hideButton.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            logger.debug("Полноэкранный режим (AboutApp)");
            Stage stage = (Stage) hideButton.getScene().getWindow();
            stage.setFullScreen(!stage.isFullScreen());
        });
        minimizeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            logger.debug("Сворачивание приложения (AboutApp)");
            Stage stage = (Stage) minimizeButton.getScene().getWindow();
            stage.setIconified(true);
        });

        // Кнопки навигации
        Event.setOnAction(e -> {
            logger.debug("Переход на страницу Event из AboutApp");
            SceneSwitcher.switchScene("/com/example/myjavafxapp/fxml/page_event.fxml", getStage());
        });
        Member.setOnAction(e -> {
            logger.debug("Переход на страницу Member из AboutApp");
            SceneSwitcher.switchScene("/com/example/myjavafxapp/fxml/page_applications.fxml", getStage());
        });
        Judge.setOnAction(e -> {
            logger.debug("Переход на страницу Judge из AboutApp");
            SceneSwitcher.switchScene("/com/example/myjavafxapp/fxml/page_judge.fxml", getStage());
        });
        Breed.setOnAction(e -> {
            logger.debug("Переход на страницу Breed из AboutApp");
            SceneSwitcher.switchScene("/com/example/myjavafxapp/fxml/page_breed.fxml", getStage());
        });
        Create_pdf.setOnAction(e -> {
            logger.debug("Нажата кнопка Create_pdf (AboutApp)");
            // Генерация PDF-отчёта
            ReportGenerator generator = new ReportGenerator();
            generator.createPDF();
        });

        // Текст инструкции
        aboutText.setText("""
                ===============  DOG EXHIBITION APP ===============
                
                1. Select "Event" to view and edit event details.
                2. Select "Member" to view participants.
                   - You can add new members, edit or delete existing ones.
                3. Select "Judge" to see list of judges;
                   - Add new judge or modify existing judges, including their breed specializations.
                4. Select "Breed" to see dog breeds.
                   - View owners who have dogs of selected breed.
                5. Use "Create PDF" to export current DB data to XML,
                   then generate a PDF report.

                SHORTCUTS:
                - Ctrl+T : Open search dialog (search by #id or by name).

                Enjoy the application!
                """);

        // Настройка прокрутки
        aboutScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Кнопка вернуться на главную
        backToHome.setOnAction(e -> {
            logger.debug("Возврат на главную сцену из AboutApp");
            SceneSwitcher.switchScene("/com/example/myjavafxapp/fxml/page_home.fxml", getStage());
        });
    }

    /**
     * Удобный метод для получения текущей сцены (Stage).
     * @return текущий Stage
     */
    private Stage getStage() {
        return (Stage) aboutText.getScene().getWindow();
    }
}
