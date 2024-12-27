package com.example.myjavafxapp.database;

import com.example.myjavafxapp.models.Breed;
import com.example.myjavafxapp.models.Dog;
import com.example.myjavafxapp.models.Member;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MemberDAO {

    private static final Logger logger = Logger.getLogger(MemberDAO.class.getName());

    // При создании новых участников назначаем им event_id=1, чтобы триггеры корректно считали участников.
    // Не будем пытаться менять event_id при загрузке, полагаемся на данные в БД.

    public static List<Member> getAllMembers() {
        String query = """
            SELECT m.id AS member_id, m.name AS member_name, m.age AS member_age,
                   m.created_at, m.updated_at, m.event_id
            FROM member m
        """;
        List<Member> members = new ArrayList<>();
        try (Connection connection = DatabaseConnection.connect();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                Member member = new Member(
                        resultSet.getInt("member_id"),
                        resultSet.getString("member_name"),
                        resultSet.getInt("member_age"),
                        null, // event не загружаем, чтобы избежать рекурсии
                        resultSet.getTimestamp("created_at"),
                        resultSet.getTimestamp("updated_at")
                );
                // Собаки
                List<Dog> dogs = getDogsByMemberId(member.getId());
                member.setDogs(dogs);
                members.add(member);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching members", e);
        }
        return members;
    }

    public static boolean saveMember(Member member) {
        if (member.getName() == null || member.getName().isEmpty()) {
            logger.warning("Cannot save member: name is null or empty.");
            return false;
        }
        if (member.getAge() < 18 || member.getAge() > 99) {
            logger.warning("Cannot save member: age is out of valid range.");
            return false;
        }

        // event_id=1 по умолчанию
        String memberQuery = "INSERT INTO member (name, age, created_at, updated_at, event_id) VALUES (?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1)";
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement memberStatement = connection.prepareStatement(memberQuery, Statement.RETURN_GENERATED_KEYS)) {

            memberStatement.setString(1, member.getName());
            memberStatement.setInt(2, member.getAge());
            memberStatement.executeUpdate();

            ResultSet generatedKeys = memberStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                int memberId = generatedKeys.getInt(1);

                for (Dog dog : member.getDogs()) {
                    saveDog(new Dog(
                            0,
                            dog.getName(),
                            dog.getAge(),
                            dog.getBreed(),
                            memberId
                    ));
                }
            }
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error saving member", e);
        }
        return false;
    }

    public static boolean updateMember(Member member) {
        if (member.getName() == null || member.getName().isEmpty()) {
            logger.warning("Cannot update member: name is null or empty.");
            return false;
        }
        if (member.getAge() < 18 || member.getAge() > 99) {
            logger.warning("Cannot update member: age is out of valid range.");
            return false;
        }

        String memberQuery = "UPDATE member SET name = ?, age = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement memberStatement = connection.prepareStatement(memberQuery)) {

            memberStatement.setString(1, member.getName());
            memberStatement.setInt(2, member.getAge());
            memberStatement.setInt(3, member.getId());
            memberStatement.executeUpdate();

            deleteDogsByMemberId(member.getId());

            for (Dog dog : member.getDogs()) {
                saveDog(new Dog(
                        0,
                        dog.getName(),
                        dog.getAge(),
                        dog.getBreed(),
                        member.getId()
                ));
            }

            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating member", e);
        }
        return false;
    }

    public static boolean deleteMember(int memberId) {
        deleteDogsByMemberId(memberId);

        String query = "DELETE FROM member WHERE id = ?";
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, memberId);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting member", e);
        }
        return false;
    }

    private static void deleteDogsByMemberId(int memberId) {
        String query = "DELETE FROM dog WHERE owner_id = ?";
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, memberId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting dogs", e);
        }
    }

    public static boolean saveDog(Dog dog) {
        String query = "INSERT INTO dog (name, age, breed_id, owner_id) VALUES (?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, dog.getName());
            preparedStatement.setInt(2, dog.getAge());
            preparedStatement.setInt(3, dog.getBreed().getId());
            preparedStatement.setInt(4, dog.getOwnerId());
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error saving dog", e);
        }
        return false;
    }

    public static List<Dog> getDogsByMemberId(int memberId) {
        String query = "SELECT d.*, b.name AS breed_name FROM dog d " +
                "LEFT JOIN breed b ON d.breed_id = b.id " +
                "WHERE d.owner_id = ?";
        List<Dog> dogs = new ArrayList<>();
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, memberId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Breed breed = new Breed(resultSet.getInt("breed_id"), resultSet.getString("breed_name"));
                Dog dog = new Dog(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getInt("age"),
                        breed,
                        resultSet.getInt("owner_id")
                );
                dogs.add(dog);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching dogs", e);
        }
        return dogs;
    }

    public static int getLastInsertedMemberId() {
        String query = "SELECT MAX(id) AS last_id FROM member";
        try (Connection connection = DatabaseConnection.connect();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            if (resultSet.next()) {
                return resultSet.getInt("last_id");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching last inserted member ID", e);
        }
        return -1;
    }

    public static List<Breed> getAllBreeds() {
        String query = "SELECT * FROM breed";
        List<Breed> breeds = new ArrayList<>();
        try (Connection connection = DatabaseConnection.connect();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                Breed breed = new Breed(
                        resultSet.getInt("id"),
                        resultSet.getString("name")
                );
                breeds.add(breed);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching breeds: " + e.getMessage());
        }
        return breeds;
    }

    public static Member getMemberById(int memberId) {
        String query = """
        SELECT m.id AS member_id, m.name AS member_name, m.age AS member_age,
               m.created_at, m.updated_at, m.event_id
        FROM member m
        WHERE m.id = ?
    """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, memberId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    Member member = new Member(
                            resultSet.getInt("member_id"),
                            resultSet.getString("member_name"),
                            resultSet.getInt("member_age"),
                            null, // не загружаем event
                            resultSet.getTimestamp("created_at"),
                            resultSet.getTimestamp("updated_at")
                    );
                    List<Dog> dogs = getDogsByMemberId(member.getId());
                    member.setDogs(dogs);
                    return member;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching member by ID", e);
        }
        return null;
    }
}
