package com.hminq.quizlett.data.dto.request;

import com.hminq.quizlett.data.remote.model.LessonCategory;
import java.util.Date;
import java.util.List;

public class LessonRequest {
    private String lessonId;
    private String title;
    private LessonCategory category;
    private List<String> questionIds;
    private Date lastVisited;

    public LessonRequest() {}

    public LessonRequest(String lessonId, String title, LessonCategory category, List<String> questionIds, Date lastVisited) {
        this.lessonId = lessonId;
        this.title = title;
        this.category = category;
        this.questionIds = questionIds;
        this.lastVisited = lastVisited;
    }

    public String getLessonId() { return lessonId; }
    public void setLessonId(String lessonId) { this.lessonId = lessonId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LessonCategory getCategory() { return category; }
    public void setCategory(LessonCategory category) { this.category = category; }

    public List<String> getQuestionIds() { return questionIds; }
    public void setQuestionIds(List<String> questionIds) { this.questionIds = questionIds; }

    public Date getLastVisited() { return lastVisited; }
    public void setLastVisited(Date lastVisited) { this.lastVisited = lastVisited; }
}
