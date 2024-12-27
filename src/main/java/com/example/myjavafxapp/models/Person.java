package com.example.myjavafxapp.models;

import javafx.scene.control.Alert;

public class Person {
    private String name;
    private int age;

    // Конструктор по умолчанию
    public Person() {
    }

    // Конструктор с параметрами
    public Person(String name, int age) {
        setName(name);
        setAge(age);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        // Если имя не валидно, просто не устанавливаем его
        if (isValidName(name)) {
            this.name = name;
        }
        // Если не валидно - игнорируем, но не показываем ошибку здесь
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        // Если возраст не валиден, не устанавливаем
        if (isValidAge(age)) {
            this.age = age;
        }
        // Если не валиден - игнорируем, но не показываем ошибку здесь
    }

    // Метод для валидации всех данных сразу
    public boolean validate() {
        StringBuilder errors = new StringBuilder();

        // Проверяем имя
        if (!isValidName(this.name)) {
            errors.append("- Name must be at least 2 characters long and contain only English letters.\n");
        }

        // Проверяем возраст
        if (!isValidAge(this.age)) {
            errors.append("- Age must be a number between 18 and 99.\n");
        }

        // Если есть ошибки
        if (errors.length() > 0) {
            showValidationError(errors.toString());
            return false;
        }

        // Если ошибок нет
        return true;
    }

    // Приватные методы для проверки валидности
    private boolean isValidName(String name) {
        if (name == null || name.length() < 2) {
            return false;
        }
        return name.matches("[a-zA-Z]+"); // Проверяем, что только английские буквы
    }

    private boolean isValidAge(int age) {
        return age >= 18 && age <= 99;
    }

    // Метод для отображения сообщения об ошибке (единственное место, где показываем Alert)
    private void showValidationError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText("Invalid Input");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
