package com.example.myjavafxapp.models;

import java.util.regex.Pattern;

public class Dog {
    private int id;
    private String name;
    private int age;
    private Breed breed;
    private int ownerId;

    public Dog() {}

    public Dog(int id, String name, int age, Breed breed, int ownerId) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.breed = breed;
        this.ownerId = ownerId;
    }

    // Геттеры и сеттеры ...
    public int getId() {
        return id;
    }

    public void setId(int id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }

    public void setAge(int age) { this.age = age; }

    public Breed getBreed() { return breed; }

    public void setBreed(Breed breed) { this.breed = breed; }

    public int getOwnerId() { return ownerId; }

    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }

    /**
     * Валидирует данные собаки.
     * @param errors StringBuilder для накопления ошибок.
     * @return true, если валидация пройдена, иначе false.
     */
    public boolean validate(StringBuilder errors) {
        // Проверка имени: только англ буквы, минимум 2 символа
        if (name == null || !Pattern.matches("[a-zA-Z]{2,}", name)) {
            errors.append("- Dog's name must contain only English letters and be at least 2 characters long.\n");
            return false;
        }

        // Возраст от 1 до 15
        if (age < 1 || age > 15) {
            errors.append("- Dog's age must be an integer between 1 and 15.\n");
            return false;
        }

        // Порода не должна быть null
        if (breed == null) {
            errors.append("- Each dog must have a selected breed.\n");
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "Dog{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", breed=" + (breed != null ? breed.getName() : "None") +
                ", ownerId=" + ownerId +
                '}';
    }
}
