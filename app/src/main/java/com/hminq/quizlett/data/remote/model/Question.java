package com.hminq.quizlett.data.remote.model;

import java.util.List;

public class Question {
    private String quesId;
    private String questionText;
    private List<String> answerOptions;
    private int correctAnswerIndex;
    private Difficulty difficulty;
    private String userId; // Added userId field

    public Question() {
        // Required empty public constructor for Firebase deserialization
    }

    // Updated constructor to include userId
    public Question(String quesId, String questionText, List<String> answerOptions, int correctAnswerIndex, Difficulty difficulty, String userId) {
        this.quesId = quesId;
        this.questionText = questionText;
        this.answerOptions = answerOptions;
        this.correctAnswerIndex = correctAnswerIndex;
        this.difficulty = difficulty;
        this.userId = userId;
    }

    public String getQuesId() {
        return quesId;
    }

    public void setQuesId(String quesId) {
        this.quesId = quesId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public List<String> getAnswerOptions() {
        return answerOptions;
    }

    public void setAnswerOptions(List<String> answerOptions) {
        this.answerOptions = answerOptions;
    }

    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    public void setCorrectAnswerIndex(int correctAnswerIndex) {
        this.correctAnswerIndex = correctAnswerIndex;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    // Getter for userId (Firebase needs this)
    public String getUserId() {
        return userId;
    }

    // Setter for userId (Optional, but good practice if needed elsewhere)
    public void setUserId(String userId) {
        this.userId = userId;
    }
}