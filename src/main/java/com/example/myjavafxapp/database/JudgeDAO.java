package com.example.myjavafxapp.database;

import com.example.myjavafxapp.models.Breed;
import com.example.myjavafxapp.models.Event;
import com.example.myjavafxapp.models.Judge;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JudgeDAO {

    // Получить всех судей из базы данных
    public static List<Judge> getAllJudges() {
        List<Judge> judges = new ArrayList<>();
        String query = """
            SELECT id, name, age, experience, created_at, updated_at, breed_id, event_id
            FROM judge
        """;

        try (Connection connection = DatabaseConnection.connect();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                int age = resultSet.getInt("age");
                int experience = resultSet.getInt("experience");
                Timestamp createdAt = resultSet.getTimestamp("created_at");
                Timestamp updatedAt = resultSet.getTimestamp("updated_at");
                Integer breedId = resultSet.getObject("breed_id") != null ? resultSet.getInt("breed_id") : null;
                Integer eventId = resultSet.getObject("event_id") != null ? resultSet.getInt("event_id") : null;
                if (eventId == null) {
                    eventId = 1;
                    setJudgeEventId(id, 1);
                }

                Breed breed = (breedId != null) ? getBreedById(breedId) : null;
                Event event = (eventId != null) ? getEventById(eventId) : null;

                Judge judge = new Judge(id, name, age, experience, event, breed, createdAt, updatedAt);
                judges.add(judge);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return judges;
    }

    // Добавить нового судью
    public static boolean insertJudge(Judge judge) {
        String query = """
        INSERT INTO judge (name, age, experience, breed_id, event_id)
        VALUES (?, ?, ?, ?, 1)
        """;
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, judge.getName());
            preparedStatement.setInt(2, judge.getAge());
            preparedStatement.setInt(3, judge.getExperience());
            if (judge.getBreed() != null) {
                preparedStatement.setInt(4, judge.getBreed().getId());
            } else {
                preparedStatement.setNull(4, Types.INTEGER);
            }

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        judge.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateJudge(Judge judge) {
        setJudgeEventId(judge.getId(), 1);

        String query = """
        UPDATE judge
        SET name = ?, age = ?, experience = ?, breed_id = ?, event_id = ?
        WHERE id = ?
        """;
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, judge.getName());
            preparedStatement.setInt(2, judge.getAge());
            preparedStatement.setInt(3, judge.getExperience());

            if (judge.getBreed() != null) {
                preparedStatement.setInt(4, judge.getBreed().getId());
            } else {
                preparedStatement.setNull(4, Types.INTEGER);
            }

            preparedStatement.setInt(5, 1);
            preparedStatement.setInt(6, judge.getId());

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Удалить судью по ID
    public static boolean deleteJudgeById(int id) {
        String query = "DELETE FROM judge WHERE id = ?";
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, id);
            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Judge getJudgeById(int id) {
        String query = """
        SELECT id, name, age, experience, created_at, updated_at, breed_id, event_id
        FROM judge
        WHERE id = ?
    """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String name = resultSet.getString("name");
                int age = resultSet.getInt("age");
                int experience = resultSet.getInt("experience");
                Timestamp createdAt = resultSet.getTimestamp("created_at");
                Timestamp updatedAt = resultSet.getTimestamp("updated_at");
                Integer breedId = resultSet.getObject("breed_id") != null ? resultSet.getInt("breed_id") : null;
                Integer eventId = resultSet.getObject("event_id") != null ? resultSet.getInt("event_id") : null;

                if (eventId == null) {
                    eventId = 1;
                    setJudgeEventId(id, 1);
                }

                Breed breed = (breedId != null) ? getBreedById(breedId) : null;
                Event event = (eventId != null) ? getEventById(eventId) : null;

                return new Judge(id, name, age, experience, event, breed, createdAt, updatedAt);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Метод для получения списка пород судьи
    public static List<Breed> getJudgeBreeds(int judgeId) {
        List<Breed> breeds = new ArrayList<>();
        String sql = "SELECT b.id, b.name FROM judge_breeds jb JOIN breed b ON jb.breed_id = b.id WHERE jb.judge_id = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, judgeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    breeds.add(new Breed(rs.getInt("id"), rs.getString("name")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return breeds;
    }

    public static void insertJudgeBreeds(int judgeId, List<Integer> breedIds) {
        String sql = "INSERT INTO judge_breeds (judge_id, breed_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Integer breedId : breedIds) {
                stmt.setInt(1, judgeId);
                stmt.setInt(2, breedId);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteJudgeBreeds(int judgeId) {
        String sql = "DELETE FROM judge_breeds WHERE judge_id = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, judgeId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Вспомогательные методы
    private static Breed getBreedById(int breedId) {
        String query = "SELECT id, name FROM breed WHERE id = ?";
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, breedId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new Breed(resultSet.getInt("id"), resultSet.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Event getEventById(int eventId) {
        String query = """
            SELECT id, location, event_time, participant_count, judge_count, winner_id, prize_pool, created_at, updated_at
            FROM event
            WHERE id = ?
        """;
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, eventId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new Event(
                        resultSet.getInt("id"),
                        resultSet.getString("location"),
                        resultSet.getTimestamp("event_time"),
                        resultSet.getInt("participant_count"),
                        resultSet.getInt("judge_count"),
                        null,
                        resultSet.getBigDecimal("prize_pool")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void setJudgeEventId(int judgeId, int eventId) {
        String updateSql = "UPDATE judge SET event_id = ? WHERE id = ?";
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(updateSql)) {
            stmt.setInt(1, eventId);
            stmt.setInt(2, judgeId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Unable to set event_id for judge ID="+judgeId+": "+e.getMessage());
        }
    }
}
