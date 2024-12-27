package com.example.myjavafxapp.models;

import javafx.scene.control.Alert;

import java.sql.Timestamp;

public class Judge extends Person {
    private int id;
    private Event event; // Связь с событием
    private Breed breed; // Связь с породой
    private int experience; // Опыт
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Конструктор с параметрами
    public Judge(int id, String name, int age, int experience, Event event, Breed breed, Timestamp createdAt, Timestamp updatedAt) {
        super(name, age);
        this.id = id;
        this.experience = experience;
        this.event = event;
        this.breed = breed;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Конструктор по умолчанию
    public Judge() {
        super("", 0);
        this.id = 0;
        this.experience = 0;
        this.event = null;
        this.breed = null;
        this.createdAt = null;
        this.updatedAt = null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Breed getBreed() {
        return breed;
    }

    public void setBreed(Breed breed) {
        this.breed = breed;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Проверяет данные судьи на корректность.
     * Сначала вызываем валидацию Person (имя и возраст),
     * затем проверяем опыт:
     * - Опыт не может быть меньше 1 года.
     * - Опыт не может быть больше (age - 18).
     */
    public boolean validate() {
        // Сначала валидируем имя и возраст через родительский метод
        if (!super.validate()) {
            return false; // Если имя или возраст некорректны, сообщение уже показано
        }

        // Проверяем опыт
        if (experience < 1) {
            showValidationError("Experience must be at least 1 year.");
            return false;
        }

        int maxExperience = getAge() - 18;
        if (experience > maxExperience) {
            showValidationError("Experience cannot exceed (age - 18) years.");
            return false;
        }

        return true;
    }

    // Метод для отображения ошибки валидации
    private void showValidationError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText("Invalid Input");
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public String toString() {
        return "Judge{" +
                "id=" + id +
                ", name='" + getName() + '\'' +
                ", age=" + getAge() +
                ", experience=" + experience +
                ", event=" + (event != null ? event.getLocation() : "None") +
                ", breed=" + (breed != null ? breed.getName() : "None") +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
