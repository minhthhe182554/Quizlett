package com.hminq.quizlett.data.remote.model;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.List;

@IgnoreExtraProperties
public class Lesson {
    private String lessonId;
    private String title;
    private String userId;
    private String creatorImage;
    private LessonCategory category;
    private int visitCount;
    private List<Question> questions;

    public Lesson() {}

    public Lesson(String title, String userId, LessonCategory category) {
        this.title = title;
        this.userId = userId;
        this.category = category;
        this.visitCount = 0;
    }

    public String getLessonId() { return lessonId; }
    public void setLessonId(String lessonId) { this.lessonId = lessonId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public LessonCategory getCategory() { return category; }
    public void setCategory(LessonCategory category) { this.category = category; }

    public int getVisitCount() { return visitCount; }
    public void setVisitCount(int visitCount) { this.visitCount = visitCount; }

    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }

    public String getCreatorImage() {
        return creatorImage;
    }

    public void setCreatorImage(String creatorImage) {
        this.creatorImage = creatorImage;
    }
}