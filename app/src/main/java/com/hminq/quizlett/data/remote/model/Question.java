package com.hminq.quizlett.data.remote.model;

import java.util.List;

public class Question {
    private String quesId;
    private String questionText;
    private List<String> answerOptions;
    private int correctAnswerIndex;
    private LessonCategory category;
    private String userId;

    public Question() {

    }

    public Question(String quesId, String questionText, List<String> answerOptions, int correctAnswerIndex, LessonCategory category, String userId) {
        this.quesId = quesId;
        this.questionText = questionText;
        this.answerOptions = answerOptions;
        this.correctAnswerIndex = correctAnswerIndex;
        this.category = category;
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

    public LessonCategory getCategory() {
        return category;
    }

    public void setCategory(LessonCategory category) {
        this.category = category;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
