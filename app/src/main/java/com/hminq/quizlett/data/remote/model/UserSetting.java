package com.hminq.quizlett.data.remote.model;

public class UserSetting {
    private Language language;
    private boolean pushNotification;

    public UserSetting() {
        this.language = Language.ENGLISH;  // default
        this.pushNotification = false;
    }

    public UserSetting(Language language, boolean pushNotification) {
        this.language = language;
        this.pushNotification = pushNotification;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public boolean isPushNotification() {
        return pushNotification;
    }

    public void setPushNotification(boolean pushNotification) {
        this.pushNotification = pushNotification;
    }
}

