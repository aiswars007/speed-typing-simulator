package com.speedtyping.dao;

import com.speedtyping.config.DatabaseConfig;
import com.speedtyping.model.TypingResult;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultDAO {

    public boolean saveResult(TypingResult result) {
        String sql = "INSERT INTO typing_results " +
                "(user_id, wpm, raw_wpm, accuracy, correct_chars, incorrect_chars, total_chars, duration_seconds, text_title) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, result.getUserId());
            ps.setInt(2, result.getWpm());
            ps.setInt(3, result.getRawWpm());
            ps.setDouble(4, result.getAccuracy());
            ps.setInt(5, result.getCorrectChars());
            ps.setInt(6, result.getIncorrectChars());
            ps.setInt(7, result.getTotalChars());
            ps.setInt(8, result.getDurationSeconds());
            ps.setString(9, result.getTextTitle() == null ? "Standard Test" : result.getTextTitle());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        result.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<TypingResult> getUserResults(int userId, int limit) {
        List<TypingResult> list = new ArrayList<>();
        String sql = "SELECT r.*, u.username FROM typing_results r " +
                "JOIN users u ON r.user_id = u.id " +
                "WHERE r.user_id = ? ORDER BY r.test_timestamp DESC LIMIT ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToResult(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<TypingResult> getLeaderboard(int limit) {
        List<TypingResult> list = new ArrayList<>();
        // Query each user's single best test result (highest WPM, highest accuracy, most recent)
        String sql = "SELECT ranked.id, ranked.user_id, u.username, ranked.wpm, ranked.raw_wpm, " +
                "ranked.accuracy, ranked.correct_chars, ranked.incorrect_chars, ranked.total_chars, " +
                "ranked.duration_seconds, ranked.text_title, ranked.test_timestamp " +
                "FROM (" +
                "    SELECT r.*, " +
                "           ROW_NUMBER() OVER (PARTITION BY r.user_id ORDER BY r.wpm DESC, r.accuracy DESC, r.test_timestamp DESC, r.id DESC) AS rn " +
                "    FROM typing_results r" +
                ") ranked " +
                "JOIN users u ON ranked.user_id = u.id " +
                "WHERE ranked.rn = 1 " +
                "ORDER BY ranked.wpm DESC, ranked.accuracy DESC, ranked.test_timestamp DESC " +
                "LIMIT ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToResult(rs));
                }
            }
        } catch (SQLException e) {
            // Fallback for database engines lacking window functions
            String fallbackSql = "SELECT r.*, u.username FROM typing_results r " +
                    "JOIN users u ON r.user_id = u.id " +
                    "WHERE r.id = (" +
                    "    SELECT r2.id FROM typing_results r2 " +
                    "    WHERE r2.user_id = r.user_id " +
                    "    ORDER BY r2.wpm DESC, r2.accuracy DESC, r2.test_timestamp DESC, r2.id DESC " +
                    "    LIMIT 1" +
                    ") " +
                    "ORDER BY r.wpm DESC, r.accuracy DESC, r.test_timestamp DESC " +
                    "LIMIT ?";
            try (Connection conn2 = DatabaseConfig.getConnection();
                 PreparedStatement ps2 = conn2.prepareStatement(fallbackSql)) {
                ps2.setInt(1, limit);
                try (ResultSet rs2 = ps2.executeQuery()) {
                    while (rs2.next()) {
                        list.add(mapResultSetToResult(rs2));
                    }
                }
            } catch (SQLException ex2) {
                ex2.printStackTrace();
            }
        }
        return list;
    }

    public boolean clearUserHistory(int userId) {
        String sql = "DELETE FROM typing_results WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Map<String, Object> getUserSummaryStats(int userId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTests", 0);
        stats.put("bestWpm", 0);
        stats.put("avgWpm", 0.0);
        stats.put("avgAccuracy", 0.0);

        String sql = "SELECT COUNT(*) AS total_tests, " +
                "MAX(wpm) AS best_wpm, " +
                "AVG(wpm) AS avg_wpm, " +
                "AVG(accuracy) AS avg_accuracy " +
                "FROM typing_results WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.put("totalTests", rs.getInt("total_tests"));
                    stats.put("bestWpm", rs.getInt("best_wpm"));
                    stats.put("avgWpm", Math.round(rs.getDouble("avg_wpm") * 10.0) / 10.0);
                    stats.put("avgAccuracy", Math.round(rs.getDouble("avg_accuracy") * 10.0) / 10.0);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    private TypingResult mapResultSetToResult(ResultSet rs) throws SQLException {
        return new TypingResult(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("username"),
                rs.getInt("wpm"),
                rs.getInt("raw_wpm"),
                rs.getDouble("accuracy"),
                rs.getInt("correct_chars"),
                rs.getInt("incorrect_chars"),
                rs.getInt("total_chars"),
                rs.getInt("duration_seconds"),
                rs.getString("text_title"),
                rs.getTimestamp("test_timestamp")
        );
    }
}
