package com.hminq.quizlett.ui;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hminq.quizlett.data.remote.model.Lesson;

/**
 * ViewModel chia sẻ trạng thái điều hướng giữa các tab.
 */
public class MainTabViewModel extends ViewModel {
    public static final int TAB_INDEX_FIRST = 0;
    public static final int TAB_INDEX_SECOND = 1;
    public static final int TAB_INDEX_THIRD = 2;

    private final MutableLiveData<Integer> targetTab = new MutableLiveData<>();
    private final MutableLiveData<Lesson> lessonToSave = new MutableLiveData<>();
    private final MutableLiveData<Lesson> lessonToView = new MutableLiveData<>();

    public LiveData<Integer> getTargetTab() {
        return targetTab;
    }

    public LiveData<Lesson> getLessonToSave() {
        return lessonToSave;
    }

    public LiveData<Lesson> getLessonToView() {
        return lessonToView;
    }

    public void requestOpenTab(int index) {
        targetTab.setValue(index);
    }

    public void requestSaveLessonToFolder(@Nullable Lesson lesson) {
        lessonToSave.setValue(lesson);
        if (lesson != null) {
            requestOpenTab(TAB_INDEX_THIRD);
        }
    }

    public void requestViewLessonDetail(@Nullable Lesson lesson) {
        lessonToView.setValue(lesson);
        if (lesson != null) {
            requestOpenTab(TAB_INDEX_FIRST);
        }
    }

    public void clearTargetTab() {
        targetTab.setValue(null);
    }

    public void clearLessonToSave() {
        lessonToSave.setValue(null);
    }

    public void clearLessonToView() {
        lessonToView.setValue(null);
    }
}

