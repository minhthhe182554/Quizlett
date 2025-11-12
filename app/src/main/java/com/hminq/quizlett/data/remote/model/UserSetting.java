package com.hminq.quizlett.data.remote.model;

public class UserSetting {
    private Language language;

    public UserSetting() {
        this.language = Language.ENGLISH;
    }

    public UserSetting(Language language) {
        this.language = language;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

}