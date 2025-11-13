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

    private List<String> lessonIds;

    private int lessonCount;

    public Folder() {
        this.lessonIds = new ArrayList<>();
    }

    public Folder(String name, String userId, String userName, Date createdAt) {
        this();
        this.name = name;
        this.userId = userId;
        this.userName = userName;
        this.createdAt = createdAt;
        this.lessonCount = 0;
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

    public List<String> getLessonIds() { return lessonIds; }

    public void setLessonIds(List<String> lessonIds) {
        this.lessonIds = lessonIds;
    }

    public int getLessonCount() {
        return lessonCount;
    }

    public void setLessonCount(int lessonCount) {
        this.lessonCount = lessonCount;
    }

    public void addLessonId(String lessonId) {
        if (this.lessonIds == null) {
            this.lessonIds = new ArrayList<>();
            this.lessonIds.add(lessonId);
        }
        if (!this.lessonIds.contains(lessonId)) {
            this.lessonIds.add(lessonId);
            this.lessonCount = this.lessonIds.size();
        }
    }

    public boolean containsLessonId(String lessonId) {
        return this.lessonIds != null && this.lessonIds.contains(lessonId);
    }

}