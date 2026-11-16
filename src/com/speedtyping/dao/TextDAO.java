package com.speedtyping.dao;

import com.speedtyping.config.DatabaseConfig;
import com.speedtyping.model.TypingText;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TextDAO {

    private static final List<TypingText> FALLBACK_TEXTS = new ArrayList<>(List.of(
            new TypingText(1, "The Quick Brown Fox", "Easy",
                    "The quick brown fox jumps over the lazy dog. Pack my box with five dozen liquor jugs. How vexingly quick daft zebras jump! Sphinx of black quartz, judge my vow. Two driven jocks help fax my big quiz."),
            new TypingText(2, "The Journey of Learning", "Easy",
                    "Practice makes progress. Every master was once a beginner who refused to quit. When you focus on accuracy first, speed naturally follows. Keep your fingers curved gently over the home row and maintain a steady rhythm as you type."),
            new TypingText(3, "Nature and Mountains", "Medium",
                    "The morning sun began to pierce through the thick canopy of ancient pine trees, casting long golden shadows across the forest floor. A gentle breeze carried the crisp fragrance of cedar and damp earth. Far in the distance, snow-capped peaks stood majestic against a clear sapphire sky."),
            new TypingText(4, "The Art of Programming", "Medium",
                    "Programming is the modern craft of thinking with clarity and precision. A software engineer shapes complex ideas into elegant logic, creating systems that connect people, automate arduous tasks, and open new horizons of innovation and creativity across the globe.")
    ));

    public List<TypingText> getAllTexts() {
        List<TypingText> list = new ArrayList<>();
        String sql = "SELECT id, title, difficulty, content FROM typing_texts ORDER BY id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new TypingText(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("difficulty"),
                        rs.getString("content")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Could not load texts from DB, using fallback texts: " + e.getMessage());
        }

        if (list.isEmpty()) {
            return new ArrayList<>(FALLBACK_TEXTS);
        }
        return list;
    }

    public TypingText getRandomText() {
        List<TypingText> all = getAllTexts();
        if (all.isEmpty()) {
            return FALLBACK_TEXTS.get(0);
        }
        return all.get(new Random().nextInt(all.size()));
    }

    /**
     * Inserts a new typing passage into the database (or fallback list).
     *
     * @param text the TypingText to add
     * @return the generated or assigned text ID, or -1 on error
     */
    public int addText(TypingText text) {
        if (text == null) return -1;

        String sql = "INSERT INTO typing_texts (title, difficulty, content) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, text.getTitle());
            ps.setString(2, text.getDifficulty() != null ? text.getDifficulty() : "Medium");
            ps.setString(3, text.getContent());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int generatedId = rs.getInt(1);
                        text.setId(generatedId);
                        synchronized (FALLBACK_TEXTS) {
                            FALLBACK_TEXTS.add(text);
                        }
                        return generatedId;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Could not insert text into database: " + e.getMessage());
        }

        // Fallback for offline mode
        synchronized (FALLBACK_TEXTS) {
            int newId = FALLBACK_TEXTS.size() + 1;
            text.setId(newId);
            FALLBACK_TEXTS.add(text);
            return newId;
        }
    }

    public TypingText getTextById(int id) {
        String sql = "SELECT id, title, difficulty, content FROM typing_texts WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new TypingText(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("difficulty"),
                            rs.getString("content")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Could not find text by id: " + e.getMessage());
        }

        synchronized (FALLBACK_TEXTS) {
            for (TypingText t : FALLBACK_TEXTS) {
                if (t.getId() == id) return t;
            }
        }
        return null;
    }

    public boolean deleteText(int id) {
        String sql = "DELETE FROM typing_texts WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int affected = ps.executeUpdate();
            synchronized (FALLBACK_TEXTS) {
                FALLBACK_TEXTS.removeIf(t -> t.getId() == id);
            }
            return affected > 0;
        } catch (SQLException e) {
            System.err.println("Could not delete text from DB: " + e.getMessage());
            synchronized (FALLBACK_TEXTS) {
                return FALLBACK_TEXTS.removeIf(t -> t.getId() == id);
            }
        }
    }
}
