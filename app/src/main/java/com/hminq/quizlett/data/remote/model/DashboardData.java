package com.hminq.quizlett.data.remote.model;

public class DashboardData {
    private int visitCount;
    private int lessonCount;
    private int questionCount;
    private int sciencePercentage; // 0-100
    private int humanitiesPercentage; // 0-100
    private int othersPercentage; // 0-100

    public DashboardData(int visitCount, int lessonCount, int questionCount, int sciencePercentage, int humanitiesPercentage, int othersPercentage) {
        this.visitCount = visitCount;
        this.lessonCount = lessonCount;
        this.questionCount = questionCount;
        this.sciencePercentage = sciencePercentage;
        this.humanitiesPercentage = humanitiesPercentage;
        this.othersPercentage = othersPercentage;
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

    public int getSciencePercentage() {
        return sciencePercentage;
    }

    public void setSciencePercentage(int sciencePercentage) {
        this.sciencePercentage = sciencePercentage;
    }

    public int getHumanitiesPercentage() {
        return humanitiesPercentage;
    }

    public void setHumanitiesPercentage(int humanitiesPercentage) {
        this.humanitiesPercentage = humanitiesPercentage;
    }

    public int getOthersPercentage() {
        return othersPercentage;
    }

    public void setOthersPercentage(int othersPercentage) {
        this.othersPercentage = othersPercentage;
    }
}
