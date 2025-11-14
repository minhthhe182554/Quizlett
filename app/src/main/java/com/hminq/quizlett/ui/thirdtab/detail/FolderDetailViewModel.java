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
import com.hminq.quizlett.data.repository.FolderRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class FolderDetailViewModel extends ViewModel {

    private static final String TAG = "FolderDetailViewModel";

    private final FirebaseDatabase db = FirebaseDatabase.getInstance();
    private final FolderRepository folderRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private DatabaseReference folderRef;
    private ValueEventListener folderListener;
    
    @Inject
    public FolderDetailViewModel(FolderRepository folderRepository) {
        this.folderRepository = folderRepository;
    }


    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading() { return _isLoading; }

    private final MutableLiveData<Folder> _folder = new MutableLiveData<>();
    public LiveData<Folder> getFolder() { return _folder; }

    private final MutableLiveData<List<Lesson>> _lessons = new MutableLiveData<>();
    public LiveData<List<Lesson>> getLessons() { return _lessons; }

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> getErrorMessage() { return _errorMessage; }
    
    private final MutableLiveData<Boolean> _deleteSuccess = new MutableLiveData<>();
    public LiveData<Boolean> getDeleteSuccess() { return _deleteSuccess; }
    
    private final MutableLiveData<Boolean> _removeLessonSuccess = new MutableLiveData<>();
    public LiveData<Boolean> getRemoveLessonSuccess() { return _removeLessonSuccess; }

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
        Log.d(TAG, "Fetching " + lessonIds.size() + " lessons by IDs: " + lessonIds);
        DatabaseReference lessonsDbRef = db.getReference("lessons");

        final List<Lesson> lessonsList = Collections.synchronizedList(new ArrayList<>());

        final AtomicInteger fetchCounter = new AtomicInteger(lessonIds.size());

        for (String id : lessonIds) {
            Log.d(TAG, "Fetching lesson ID: " + id);
            lessonsDbRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.d(TAG, "Snapshot exists for " + id + ": " + snapshot.exists());
                    if (snapshot.exists()) {
                        Lesson lesson = snapshot.getValue(Lesson.class);
                        if (lesson != null) {
                            lesson.setLessonId(snapshot.getKey());
                            Log.d(TAG, "✅ Loaded lesson: " + lesson.getTitle() + ", questions: " + 
                                (lesson.getQuestions() != null ? lesson.getQuestions().size() : "null"));
                            lessonsList.add(lesson);
                        } else {
                            Log.w(TAG, "⚠️ Lesson object is null for ID: " + id);
                        }
                    } else {
                        Log.w(TAG, "❌ Lesson với ID " + id + " không tồn tại.");
                    }

                    if (fetchCounter.decrementAndGet() == 0) {
                        Log.d(TAG, "✅ Đã tải xong tất cả " + lessonsList.size() + " lessons.");
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

    public void deleteFolder(String folderId) {
        _isLoading.setValue(true);
        
        // Remove listener BEFORE deleting to prevent "folder not found" error message
        if (folderRef != null && folderListener != null) {
            Log.d(TAG, "Removing folder listener before deletion");
            folderRef.removeEventListener(folderListener);
            folderListener = null;
        }
        
        disposables.add(
            folderRepository.deleteFolder(folderId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> {
                        Log.d(TAG, "Folder deleted successfully");
                        _isLoading.setValue(false);
                        _deleteSuccess.setValue(true);
                    },
                    throwable -> {
                        Log.e(TAG, "Failed to delete folder: " + throwable.getMessage());
                        _isLoading.setValue(false);
                        _errorMessage.setValue("Failed to delete folder: " + throwable.getMessage());
                        _deleteSuccess.setValue(false);
                    }
                )
        );
    }
    
    public void removeLessonFromFolder(String folderId, String lessonId) {
        disposables.add(
            folderRepository.removeLessonFromFolder(folderId, lessonId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> {
                        Log.d(TAG, "Lesson removed from folder successfully");
                        _removeLessonSuccess.setValue(true);
                    },
                    throwable -> {
                        Log.e(TAG, "Failed to remove lesson: " + throwable.getMessage());
                        _removeLessonSuccess.setValue(false);
                    }
                )
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "Hủy Listener Realtime Database.");
        if (folderRef != null && folderListener != null) {
            folderRef.removeEventListener(folderListener);
        }
        disposables.clear();
    }
}