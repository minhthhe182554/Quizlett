package com.hminq.quizlett.data.remote.model;

public enum Language {
    ENGLISH("en", "English"),
    VIETNAMESE("vi", "Tiếng Việt");

    private final String code;
    private final String displayName;

    Language(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Language fromCode(String code) {
        for (Language lang : values()) {
            if (lang.code.equalsIgnoreCase(code)) {
                return lang;
            }
        }
        return ENGLISH;
    }

    public static int getArrayIndex(String langCode) {
        Language lang = fromCode(langCode);
        if (lang == VIETNAMESE) {
            return 1;
        }
        return 0;
    }
}