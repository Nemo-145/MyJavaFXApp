package com.example.myjavafxapp.controllers;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class BreedControllerTest {

    private Breed_controller controller;

    @BeforeEach
    public void setUp() throws Exception {
        // Запуск приложения для инициализации потока JavaFX
        Application.launch(TestApplication.class);
    }

    @Test
    public void testLoadDogBreedsFromXML() throws Exception {
        // Путь к XML-файлу
        String filePath = "/com/example/myjavafxapp/data/breed.xml";

        // Используем рефлексию для вызова приватного метода
        Method method = Breed_controller.class.getDeclaredMethod("loadDogBreedsFromXML", String.class);
        method.setAccessible(true);

        // Вызов метода с путем к XML-файлу
        method.invoke(controller, filePath);

        // Получаем таблицу через геттер
        TableView<DogBreed> breedTableView = controller.getBreedTableView();

        ObservableList<DogBreed> dogBreeds = breedTableView.getItems();

        // Проверка результатов
        assertEquals(3, dogBreeds.size(), "Должно быть загружено 3 породы.");
        assertEquals("Bulldog", dogBreeds.get(0).getBreed());
        assertEquals("Labrador", dogBreeds.get(1).getBreed());
        assertEquals("Beagle", dogBreeds.get(2).getBreed());
    }

    // Класс для инициализации JavaFX в тесте
    public static class TestApplication extends Application {
        @Override
        public void start(Stage stage) throws Exception {
            // Загружаем FXML в тесте с правильным путем
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/myjavafxapp/fxml/breed.fxml"));
            Parent root = loader.load();  // Загружаем FXML без указания контроллера

            // Инициализация контроллера после загрузки FXML
            Breed_controller controller = loader.getController();  // Получаем контроллер из FXML
            controller.initialize();  // Инициализация контроллера

            // Устанавливаем сцену, хотя в тестах она не отображается
            stage.setScene(new Scene(root));
            stage.show();
        }
    }
}
