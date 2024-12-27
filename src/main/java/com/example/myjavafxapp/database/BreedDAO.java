package com.example.myjavafxapp.database;

import com.example.myjavafxapp.models.Breed;
import com.example.myjavafxapp.models.Member;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BreedDAO {

    // Добавить новую породу
    public static void insertBreed(String name) {
        String query = "INSERT INTO breed (name) VALUES (?)";
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, name);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Получить все породы
    public static List<String> getAllBreeds() {
        String query = "SELECT name FROM breed";
        List<String> breeds = new ArrayList<>();
        try (Connection connection = DatabaseConnection.connect();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                breeds.add(resultSet.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return breeds;
    }

    // Удалить породу по ID
    public static void deleteBreedById(int id) {
        String query = "DELETE FROM breed WHERE id = ?";
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Member> getOwnersByBreed(String breedName) {
        List<Member> owners = new ArrayList<>();
        String query = """
        SELECT m.id, m.name, m.age, m.event_id, m.created_at, m.updated_at
        FROM member m
        JOIN dog d ON d.owner_id = m.id
        JOIN breed b ON d.breed_id = b.id
        WHERE b.name = ?
        """;
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, breedName);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Member member = new Member(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getInt("age"),
                        null, // Event, если требуется, можно добавить
                        resultSet.getTimestamp("created_at"),
                        resultSet.getTimestamp("updated_at")
                );
                owners.add(member);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return owners;
    }

    public static List<Breed> getAllBreedsAsObjects() {
        String query = "SELECT id, name FROM breed";
        List<Breed> breeds = new ArrayList<>();
        try (Connection connection = DatabaseConnection.connect();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                breeds.add(new Breed(resultSet.getInt("id"), resultSet.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return breeds;
    }


}
