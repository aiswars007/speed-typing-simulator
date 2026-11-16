package com.speedtyping.model;

import java.sql.Timestamp;

public class TypingResult {
    private int id;
    private int userId;
    private String username; // for display in leaderboard
    private int wpm;
    private int rawWpm;
    private double accuracy;
    private int correctChars;
    private int incorrectChars;
    private int totalChars;
    private int durationSeconds;
    private String textTitle;
    private Timestamp testTimestamp;

    public TypingResult() {}

    public TypingResult(int id, int userId, String username, int wpm, int rawWpm, double accuracy,
                        int correctChars, int incorrectChars, int totalChars, int durationSeconds,
                        String textTitle, Timestamp testTimestamp) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.wpm = wpm;
        this.rawWpm = rawWpm;
        this.accuracy = accuracy;
        this.correctChars = correctChars;
        this.incorrectChars = incorrectChars;
        this.totalChars = totalChars;
        this.durationSeconds = durationSeconds;
        this.textTitle = textTitle;
        this.testTimestamp = testTimestamp;
    }

    public TypingResult(int userId, int wpm, int rawWpm, double accuracy,
                        int correctChars, int incorrectChars, int totalChars,
                        int durationSeconds, String textTitle) {
        this.userId = userId;
        this.wpm = wpm;
        this.rawWpm = rawWpm;
        this.accuracy = accuracy;
        this.correctChars = correctChars;
        this.incorrectChars = incorrectChars;
        this.totalChars = totalChars;
        this.durationSeconds = durationSeconds;
        this.textTitle = textTitle;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getWpm() {
        return wpm;
    }

    public void setWpm(int wpm) {
        this.wpm = wpm;
    }

    public int getRawWpm() {
        return rawWpm;
    }

    public void setRawWpm(int rawWpm) {
        this.rawWpm = rawWpm;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public int getCorrectChars() {
        return correctChars;
    }

    public void setCorrectChars(int correctChars) {
        this.correctChars = correctChars;
    }

    public int getIncorrectChars() {
        return incorrectChars;
    }

    public void setIncorrectChars(int incorrectChars) {
        this.incorrectChars = incorrectChars;
    }

    public int getTotalChars() {
        return totalChars;
    }

    public void setTotalChars(int totalChars) {
        this.totalChars = totalChars;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getTextTitle() {
        return textTitle;
    }

    public void setTextTitle(String textTitle) {
        this.textTitle = textTitle;
    }

    public Timestamp getTestTimestamp() {
        return testTimestamp;
    }

    public void setTestTimestamp(Timestamp testTimestamp) {
        this.testTimestamp = testTimestamp;
    }
}
