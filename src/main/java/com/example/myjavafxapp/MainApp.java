package com.example.myjavafxapp;

import com.example.myjavafxapp.database.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.control.Alert;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application class for a JavaFX application.
 * Запускает приложение, показывает модальное окно с просьбой
 * нажать на кнопку "About app" для чтения инструкции.
 */
public class MainApp extends Application {

    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);
    private static final int EVENT_ID = 1; // ID события для подсчёта участников/судей

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Показываем модальное окно при запуске
        showStartupDialog();

        Parent root = FXMLLoader.load(getClass().getResource("/com/example/myjavafxapp/fxml/page_home.fxml"));
        primaryStage.setTitle("My JavaFX App");
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        // Запускаем поток для периодического подсчёта участников и судей
        startCountingThread();
    }

    /**
     * Модальное окно, информирующее пользователя о нажатии "About app".
     */
    private void showStartupDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Welcome");
        alert.setHeaderText(null);
        alert.setContentText("Please press the 'About app' button on the main menu to read the instructions.");
        alert.showAndWait();
    }

    /**
     * Запускает отдельный поток (scheduler) который каждые 5 секунд
     * пересчитывает участников и судей.
     */
    private void startCountingThread() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            logger.debug("Пересчет участников и судей в потоке: {}", Thread.currentThread().getName());
            updateParticipantAndJudgeCounts(EVENT_ID);
        }, 0, 5, TimeUnit.SECONDS);
    }

    /**
     * Подсчитывает количество участников и судей для указанного события (eventId).
     * Выполняется в отдельном потоке, не в JavaFX Application Thread.
     */
    private void updateParticipantAndJudgeCounts(int eventId) {
        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn.createStatement()) {

            // Подсчет участников
            ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(*) AS cnt FROM member WHERE event_id = " + eventId);
            int participantCount = 0;
            if (rs.next()) {
                participantCount = rs.getInt("cnt");
            }

            // Подсчет судей
            rs = stmt.executeQuery(
                    "SELECT COUNT(*) AS cnt FROM judge WHERE event_id = " + eventId);
            int judgeCount = 0;
            if (rs.next()) {
                judgeCount = rs.getInt("cnt");
            }

            // Обновляем таблицу event
            String updateSql = "UPDATE event SET participant_count = " + participantCount
                    + ", judge_count = " + judgeCount
                    + " WHERE id = " + eventId;
            stmt.executeUpdate(updateSql);

            logger.debug("Обновлено событие ID={}: участники={}, судьи={}",
                    eventId, participantCount, judgeCount);

        } catch (SQLException e) {
            logger.error("Ошибка при подсчёте участников/судей", e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
