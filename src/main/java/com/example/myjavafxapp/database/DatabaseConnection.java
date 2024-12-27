package com.example.myjavafxapp.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/MyJavaFxApp"; // Замените "javafx_demo" на имя вашей базы
    private static final String USER = "root"; // Ваш логин
    private static final String PASSWORD = "root"; // Ваш пароль

    public static Connection connect() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка подключения к базе данных!");
        }
    }
}
