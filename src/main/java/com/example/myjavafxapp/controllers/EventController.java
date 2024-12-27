package com.example.myjavafxapp.controllers;

import com.example.myjavafxapp.database.EventDAO;
import com.example.myjavafxapp.database.MemberDAO;
import com.example.myjavafxapp.models.Event;
import com.example.myjavafxapp.models.Member;
import com.example.myjavafxapp.utils.DatabaseToXMLExporter;
import com.example.myjavafxapp.utils.ReportGenerator;
import com.example.myjavafxapp.utils.SceneSwitcher;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Контроллер для страницы "Event".
 * Позволяет просматривать/редактировать событие (location, prizePool, date/time),
 * а также видеть список победителей.
 */
public class EventController {

    private static final Logger logger = LoggerFactory.getLogger(EventController.class);

    @FXML private VBox Bar;
    @FXML private Button Event;
    @FXML private Button Member;
    @FXML private Button Judge;
    @FXML private Button Breed;
    @FXML private Button About_app;
    @FXML private Button Create_pdf;
    @FXML private HBox headerButtons;
    @FXML private Button closeButton;
    @FXML private Button hideButton;
    @FXML private Button minimizeButton;

    @FXML private VBox dataContainer_event;
    @FXML private TextField locationField;
    @FXML private TextField prizePoolField;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    @FXML private Label participantsCountLabel;
    @FXML private Label judgesCountLabel;
    @FXML private ListView<String> winnersListView;
    @FXML private HBox buttonContainer;

    private Event currentEvent;
    private boolean isEditMode = false;

    @FXML
    public void initialize() {
        logger.info("Инициализация EventController");

        closeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> closeApplication());
        minimizeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> minimizeApplication());
        hideButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> maximizeApplication());

        Event.setOnAction(e -> {
            // Already on Event
            showInfo("Already on Event page");
        });
        Member.setOnAction(e -> switchToScene("/com/example/myjavafxapp/fxml/page_applications.fxml"));
        Judge.setOnAction(e -> switchToScene("/com/example/myjavafxapp/fxml/page_judge.fxml"));
        Breed.setOnAction(e -> switchToScene("/com/example/myjavafxapp/fxml/page_breed.fxml"));
        About_app.setOnAction(e -> switchToScene("/com/example/myjavafxapp/fxml/page_aboutapp.fxml"));

        Create_pdf.setOnAction(event -> {
            logger.debug("Нажата кнопка PDF (Eventcontroller)");
            // 1) генерируем XML
            DatabaseToXMLExporter.exportAllTablesToXML("all_tables.xml");
            // 2) генерируем PDF
            new ReportGenerator().createPDF();
        });


        logger.debug("Загрузка события ID=1");
        currentEvent = EventDAO.getEventById(1);
        if (currentEvent == null) {
            currentEvent = new Event();
            currentEvent.setId(1);
            currentEvent.setLocation("");
            currentEvent.setPrizePool(BigDecimal.ZERO);
            currentEvent.setEventTime(java.sql.Timestamp.valueOf(LocalDateTime.now()));
        }

        restrictToDigits(prizePoolField);
        restrictLocation(locationField);

        displayEventDetails();
    }

    private void displayEventDetails() {
        locationField.setText(currentEvent.getLocation());
        prizePoolField.setText(currentEvent.getPrizePool().toString());

        LocalDateTime eventDateTime = currentEvent.getEventTime().toLocalDateTime();
        datePicker.setValue(eventDateTime.toLocalDate());
        timeField.setText(String.format("%02d:%02d", eventDateTime.getHour(), eventDateTime.getMinute()));

        // Обновляем participant_count, judge_count
        Event updatedEvent = EventDAO.getEventById(currentEvent.getId());
        if (updatedEvent != null) {
            currentEvent = updatedEvent;
        }

        participantsCountLabel.setText(String.valueOf(currentEvent.getParticipantCount()));
        judgesCountLabel.setText(String.valueOf(currentEvent.getJudgeCount()));

        // Winners
        List<Member> winners = EventDAO.getEventWinners(currentEvent.getId());
        winnersListView.getItems().clear();
        for (Member w : winners) {
            int age = w.getAge();
            StringBuilder dogNames = new StringBuilder();
            if (w.getDogs() != null && !w.getDogs().isEmpty()) {
                for (int i = 0; i < w.getDogs().size(); i++) {
                    dogNames.append(w.getDogs().get(i).getName());
                    if (i < w.getDogs().size() - 1) dogNames.append(", ");
                }
            }
            String info = "Name: " + w.getName() + " (Age: " + age + ")";
            if (dogNames.length() > 0) {
                info += ", Dogs: " + dogNames;
            } else {
                info += ", Dogs: None";
            }
            winnersListView.getItems().add(info);
        }

        showViewModeButtons();
        setFieldsEditable(false);
    }

    private void showViewModeButtons() {
        buttonContainer.getChildren().clear();

        Button redactButton = new Button("Redact");
        redactButton.setOnAction(e -> {
            isEditMode = true;
            setFieldsEditable(true);
            showEditModeButtons();
        });

        buttonContainer.getChildren().add(redactButton);
    }

    private void showEditModeButtons() {
        buttonContainer.getChildren().clear();

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            if (validateAndSave()) {
                isEditMode = false;
                setFieldsEditable(false);
                displayEventDetails();
            }
        });

        Button cleanButton = new Button("Clean");
        cleanButton.setOnAction(e -> {
            displayEventDetails();
            isEditMode = false;
        });

        buttonContainer.getChildren().addAll(cleanButton, saveButton);
    }

    private boolean validateAndSave() {
        String location = locationField.getText().trim();
        if (location.length() < 5) {
            showError("Location must be at least 5 characters long.");
            return false;
        }

        String prizeStr = prizePoolField.getText().trim();
        BigDecimal prize;
        try {
            prize = new BigDecimal(prizeStr);
            if (prize.compareTo(BigDecimal.ZERO) <= 0) {
                showError("Prize pool must be > 0.");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Prize pool must be a valid number.");
            return false;
        }

        LocalDate chosenDate = datePicker.getValue();
        if (chosenDate == null || chosenDate.isBefore(LocalDate.now()) || chosenDate.isAfter(LocalDate.now().plusYears(2))) {
            showError("Date must be between today and 2 years later.");
            return false;
        }

        String time = timeField.getText().trim();
        if (!time.matches("\\d{2}:\\d{2}")) {
            showError("Time must be in HH:MM format.");
            return false;
        }
        String[] parts = time.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
            showError("Invalid time.");
            return false;
        }

        LocalDateTime eventDateTime = LocalDateTime.of(chosenDate, LocalTime.of(hour, minute));
        if (eventDateTime.isBefore(LocalDateTime.now())) {
            showError("Cannot schedule event in the past.");
            return false;
        }

        currentEvent.setLocation(location);
        currentEvent.setPrizePool(prize);
        currentEvent.setEventTime(java.sql.Timestamp.valueOf(eventDateTime));

        boolean updated = EventDAO.updateEvent(currentEvent);
        if (!updated) {
            showError("Failed to update event in DB.");
            return false;
        }

        return true;
    }

    private void setFieldsEditable(boolean editable) {
        locationField.setDisable(!editable);
        prizePoolField.setDisable(!editable);
        datePicker.setDisable(!editable);
        timeField.setDisable(!editable);

        participantsCountLabel.setDisable(true);
        judgesCountLabel.setDisable(true);
        winnersListView.setDisable(true);
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText("Invalid Input");
        alert.setContentText(message);
        alert.showAndWait();
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

    private void switchToScene(String fxmlPath) {
        Stage currentStage = (Stage) Event.getScene().getWindow();
        SceneSwitcher.switchScene(fxmlPath, currentStage);
    }

    private void restrictToDigits(TextField textField) {
        textField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            if (!event.getCharacter().matches("[0-9]")) {
                event.consume();
            }
        });
    }

    private void restrictLocation(TextField textField) {
        Pattern pattern = Pattern.compile("[a-zA-Z0-9 .,-]*");
        textField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            if (!pattern.matcher(event.getCharacter()).matches()) {
                event.consume();
            }
        });
    }
}
