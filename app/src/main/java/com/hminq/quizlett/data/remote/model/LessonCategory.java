package com.hminq.quizlett.data.remote.model;

import android.content.Context;

import com.hminq.quizlett.R;

public enum LessonCategory {
    Sciences,
    Humanities,
    Others;

    public int getStringResId() {
        switch (this) {
            case Sciences:
                return R.string.category_sciences;
            case Humanities:
                return R.string.category_humanities;
            case Others:
                return R.string.category_others;
            default:
                return R.string.category_others;
        }
    }

    public String getLocalizedName(Context context) {
        return context.getString(getStringResId());
    }
}