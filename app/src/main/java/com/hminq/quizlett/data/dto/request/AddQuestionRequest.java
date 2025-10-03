package com.hminq.quizlett.data.dto.request;

import com.hminq.quizlett.data.remote.model.Difficulty;

import java.util.List;

public class AddQuestionRequest {
    private String questionText;
    private List<String> answerOptions;
    private int correctAnswerIndex;
    private Difficulty difficulty;

    public AddQuestionRequest() {
    }

    public AddQuestionRequest(String questionText, List<String> answerOptions, int correctAnswerIndex, Difficulty difficulty) {
        this.questionText = questionText;
        this.answerOptions = answerOptions;
        this.correctAnswerIndex = correctAnswerIndex;
        this.difficulty = difficulty;
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
}