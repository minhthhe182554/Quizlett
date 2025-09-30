package com.hminq.quizlett.utils;

import android.view.View;

import com.google.android.material.snackbar.Snackbar;

public class Message {
    public static void showShort(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

    public static void showLong(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }
}
