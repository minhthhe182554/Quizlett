package com.hminq.quizlett.data.remote.model;

import com.hminq.quizlett.constants.UserConstant;

public class User {
    private String uid;
    private String email;
    private String password;
    private String fullname;
    private String profileImageUrl;
    private int streak;
    private UserSetting userSetting;

    public User() {}

    public User(String uid, String email, String password, String fullname) {
        this.uid = uid;
        this.email = email;
        this.password = password;
        this.fullname = fullname;
        this.profileImageUrl = UserConstant.DEFAULT_IMG_URL;
        this.streak = UserConstant.DEFAULT_STREAK;
        this.userSetting = UserConstant.DEFAULT_SETTING;
    }

    public User(String uid, String email, String password, String fullname, String profileImageUrl, int streak, UserSetting userSetting) {
        this.uid = uid;
        this.email = email;
        this.password = password;
        this.fullname = fullname;
        this.profileImageUrl = profileImageUrl;
        this.streak = streak;
        this.userSetting = userSetting;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public int getStreak() {
        return streak;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }

    public UserSetting getUserSetting() {
        return userSetting;
    }

    public void setUserSetting(UserSetting userSetting) {
        this.userSetting = userSetting;
    }

    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", fullname='" + fullname + '\'' +
                ", profileImageUrl='" + profileImageUrl + '\'' +
                ", streak=" + streak +
                ", userSetting=" + userSetting +
                '}';
    }
}
