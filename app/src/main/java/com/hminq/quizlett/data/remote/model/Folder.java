package com.hminq.quizlett.data.remote.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Folder {

    private String folderId;
    private String name;
    private String userId;
    private String userName;
    private Date createdAt;

    private List<Lesson> lessons;

    public Folder() {
        this.lessons = new ArrayList<>();
    }

    public Folder(String name, String userId, String userName, Date createdAt) {
        this();
        this.name = name;
        this.userId = userId;
        this.userName = userName;
        this.createdAt = createdAt;
    }

    public String getFolderId() { return folderId; }
    public void setFolderId(String folderId) { this.folderId = folderId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; } 

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public List<Lesson> getLessons() { return lessons; }
    public void setLessons(List<Lesson> lessons) {
        this.lessons = lessons;
    }
    public int getLessonCount() {
        return (lessons != null) ? lessons.size() : 0;
    }
    public void addLesson(Lesson lesson) {
        if (this.lessons == null) {
            this.lessons = new ArrayList<>();
        }
        this.lessons.add(lesson);
    }
}