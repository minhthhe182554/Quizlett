package com.hminq.quizlett.data.remote.model;

import java.util.Map;

public class DashboardData {
    private int visitCount;
    private int lessonCount;
    private int questionCount;
    private Map<String, Float> categoryPercentage;

    public DashboardData(int visitCount, int lessonCount, int questionCount, Map<String, Float> categoryPercentage) {
        this.visitCount = visitCount;
        this.lessonCount = lessonCount;
        this.questionCount = questionCount;
        this.categoryPercentage = categoryPercentage;
    }

    public int getVisitCount() {
        return visitCount;
    }

    public void setVisitCount(int visitCount) {
        this.visitCount = visitCount;
    }

    public int getLessonCount() {
        return lessonCount;
    }

    public void setLessonCount(int lessonCount) {
        this.lessonCount = lessonCount;
    }

    public int getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(int questionCount) {
        this.questionCount = questionCount;
    }

    public Map<String, Float> getCategoryPercentage() {
        return categoryPercentage;
    }

    public void setCategoryPercentage(Map<String, Float> categoryPercentage) {
        this.categoryPercentage = categoryPercentage;
    }
}
