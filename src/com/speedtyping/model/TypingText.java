package com.speedtyping.model;

public class TypingText {
    private int id;
    private String title;
    private String difficulty;
    private String content;

    public TypingText() {}

    public TypingText(int id, String title, String difficulty, String content) {
        this.id = id;
        this.title = title;
        this.difficulty = difficulty;
        this.content = content;
    }

    public TypingText(String title, String difficulty, String content) {
        this.title = title;
        this.difficulty = difficulty;
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return title + " (" + difficulty + ")";
    }
}
