package com.hminq.quizlett.data.dto.request;

import com.hminq.quizlett.data.remote.model.LessonCategory;

import java.util.List;

public class AddQuestionRequest {
    private String questionText;
    private List<String> answerOptions;
    private int correctAnswerIndex;
    private LessonCategory category;

    public AddQuestionRequest() {
    }

    public AddQuestionRequest(String questionText, List<String> answerOptions, int correctAnswerIndex, LessonCategory category) {
        this.questionText = questionText;
        this.answerOptions = answerOptions;
        this.correctAnswerIndex = correctAnswerIndex;
        this.category = category;
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

    public LessonCategory getCategory() {
        return category;
    }

    public void setCategory(LessonCategory category) {
        this.category = category;
    }
}
