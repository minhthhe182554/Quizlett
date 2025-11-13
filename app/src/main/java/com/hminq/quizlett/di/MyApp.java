package com.hminq.quizlett.di;

import android.app.Application;


import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
    }
}
