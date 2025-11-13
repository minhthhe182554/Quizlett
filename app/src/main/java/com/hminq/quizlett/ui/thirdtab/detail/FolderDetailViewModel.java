package com.hminq.quizlett.ui.thirdtab.detail;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import com.hminq.quizlett.data.remote.model.Folder;
import com.hminq.quizlett.data.remote.model.Lesson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class FolderDetailViewModel extends ViewModel {

    private static final String TAG = "FolderDetailViewModel";

    private final FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference folderRef;
    private ValueEventListener folderListener;


    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading() { return _isLoading; }

    private final MutableLiveData<Folder> _folder = new MutableLiveData<>();
    public LiveData<Folder> getFolder() { return _folder; }

    private final MutableLiveData<List<Lesson>> _lessons = new MutableLiveData<>();
    public LiveData<List<Lesson>> getLessons() { return _lessons; }

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> getErrorMessage() { return _errorMessage; }

    public void loadFolderAndLessons(String folderId) {
        _isLoading.setValue(true);
        _errorMessage.setValue(null);

        folderRef = db.getReference("folders").child(folderId);

        if (folderListener != null) {
            folderRef.removeEventListener(folderListener);
        }

        folderListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Folder folderData = snapshot.getValue(Folder.class);
                    _folder.setValue(folderData);

                    if (folderData != null && folderData.getLessonIds() != null && !folderData.getLessonIds().isEmpty()) {

                        fetchLessonsByIDs(folderData.getLessonIds());
                    } else {
                        Log.d(TAG, "Folder không có Lesson nào.");
                        _lessons.setValue(Collections.emptyList());
                        _isLoading.setValue(false);
                    }
                } else {
                    Log.d(TAG, "Folder không tồn tại.");
                    _errorMessage.setValue("Folder không tồn tại hoặc đã bị xóa.");
                    _folder.setValue(null);
                    _lessons.setValue(Collections.emptyList());
                    _isLoading.setValue(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                _isLoading.setValue(false);
                Log.e(TAG, "Lỗi khi lắng nghe Folder: " + error.getMessage());
                _errorMessage.setValue("Không thể tải thông tin Folder.");
                _folder.setValue(null);
            }
        };

        folderRef.addValueEventListener(folderListener);
    }

    private void fetchLessonsByIDs(List<String> lessonIds) {

        DatabaseReference lessonsDbRef = db.getReference("lessons");

        final List<Lesson> lessonsList = Collections.synchronizedList(new ArrayList<>());

        final AtomicInteger fetchCounter = new AtomicInteger(lessonIds.size());

        for (String id : lessonIds) {
            lessonsDbRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Lesson lesson = snapshot.getValue(Lesson.class);
                        if (lesson != null) {
                            lesson.setLessonId(snapshot.getKey());
                            lessonsList.add(lesson);
                        }
                    } else {
                        Log.w(TAG, "Lesson với ID " + id + " không tồn tại.");
                    }

                    if (fetchCounter.decrementAndGet() == 0) {
                        Log.d(TAG, "Đã tải xong tất cả " + lessonsList.size() + " lessons.");
                        _lessons.setValue(lessonsList);
                        _isLoading.setValue(false); // Hoàn tất loading
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Lỗi khi tải lesson " + id, error.toException());

                    if (fetchCounter.decrementAndGet() == 0) {
                        _lessons.setValue(lessonsList);
                        _isLoading.setValue(false);
                    }
                }
            });
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "Hủy Listener Realtime Database.");
        if (folderRef != null && folderListener != null) {
            folderRef.removeEventListener(folderListener);
        }
    }
}