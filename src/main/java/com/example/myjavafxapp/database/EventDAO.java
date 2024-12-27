package com.example.myjavafxapp.database;

import com.example.myjavafxapp.models.Event;
import com.example.myjavafxapp.models.Member;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EventDAO {

    public static Event getEventById(int eventId) {
        Event event = null;
        String sql = """
            SELECT id, location, event_time, participant_count, judge_count, winner_id, prize_pool
            FROM event
            WHERE id = ?
        """;

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, eventId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    event = new Event();
                    event.setId(rs.getInt("id"));
                    event.setLocation(rs.getString("location"));
                    event.setEventTime(rs.getTimestamp("event_time"));
                    event.setParticipantCount(rs.getInt("participant_count"));
                    event.setJudgeCount(rs.getInt("judge_count"));
                    event.setPrizePool(rs.getBigDecimal("prize_pool"));
                    // Не загружаем winner (Member), чтобы избежать рекурсии, winner можно загрузить отдельно при необходимости
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return event;
    }

    public static boolean updateWinner(int eventId, Integer winnerId) {
        // Данный метод устарел, оставим без изменений, но имейте в виду, что он заменит winner_id единственным победителем.
        String sql = "UPDATE event SET winner_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (winnerId == null) {
                stmt.setNull(1, Types.INTEGER);
            } else {
                stmt.setInt(1, winnerId);
            }
            stmt.setInt(2, eventId);

            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static List<Member> getEventWinners(int eventId) {
        List<Member> winners = new ArrayList<>();
        String sql = """
            SELECT m.id
            FROM event_winners ew
            JOIN member m ON ew.member_id = m.id
            WHERE ew.event_id = ?
        """;
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, eventId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int memberId = rs.getInt("id");
                    Member mem = MemberDAO.getMemberById(memberId);
                    if (mem != null) {
                        winners.add(mem);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return winners;
    }

    /**
     * Обновляем список победителей таким образом, чтобы добавить новых к уже существующим, не удаляя старых.
     * Если вам нужно полностью заменить список, передайте полный новый список.
     * Если нужно добавить новых победителей, сначала загрузите текущих, добавьте новых в список и вызовите updateWinners.
     */
    public static boolean updateWinners(int eventId, List<Integer> newWinnerIds) {
        try (Connection conn = DatabaseConnection.connect()) {
            // 1. Получить текущих победителей
            Set<Integer> currentWinners = new HashSet<>();
            String selectSql = "SELECT member_id FROM event_winners WHERE event_id = ?";
            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setInt(1, eventId);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    while (rs.next()) {
                        currentWinners.add(rs.getInt("member_id"));
                    }
                }
            }

            // 2. Добавить новых победителей к текущим
            currentWinners.addAll(newWinnerIds);

            // 3. Удаляем из таблицы только тех победителей, которых теперь нет в set'е
            // Сначала получим всех текущих в БД
            // Если мы хотим полностью обновить, то перед newWinnerIds передавайте полный список.
            // Здесь же мы перезаписываем winners заново: удаляем всех из event_winners и вставляем заново.
            // Так что изменим логику: полностью заменяем список (как было), но теперь будем вставлять всех.

            // Полная замена:
            String deleteSql = "DELETE FROM event_winners WHERE event_id = ?";
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, eventId);
                deleteStmt.executeUpdate();
            }

            if (currentWinners.isEmpty()) {
                return true; // нет победителей
            }

            // Вставляем весь объединённый список победителей
            String insertSql = "INSERT INTO event_winners (event_id, member_id) VALUES (?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                for (Integer winnerId : currentWinners) {
                    insertStmt.setInt(1, eventId);
                    insertStmt.setInt(2, winnerId);
                    insertStmt.addBatch();
                }
                insertStmt.executeBatch();
            }

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateEvent(Event event) {
        String sql = "UPDATE event SET location = ?, event_time = ?, prize_pool = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, event.getLocation());
            stmt.setTimestamp(2, event.getEventTime());
            stmt.setBigDecimal(3, event.getPrizePool());
            stmt.setInt(4, event.getId());

            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
