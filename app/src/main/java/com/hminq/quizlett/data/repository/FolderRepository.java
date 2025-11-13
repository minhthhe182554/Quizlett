package com.hminq.quizlett.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.hminq.quizlett.data.remote.model.Folder;
import com.hminq.quizlett.data.remote.model.Lesson; // Cần import lớp Lesson
import com.hminq.quizlett.exceptions.ValidationException;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;


public class FolderRepository {

    private final DatabaseReference foldersReference;
    private final DatabaseReference lessonsReference;
    private final FirebaseAuth firebaseAuth;
    private static final String TAG = "FOLDER_REPO";

    @Inject
    public FolderRepository(FirebaseAuth firebaseAuth,
                            FirebaseDatabase firebaseDatabase) {
        this.firebaseAuth = firebaseAuth;
        this.foldersReference = firebaseDatabase.getReference("folders");
        this.lessonsReference = firebaseDatabase.getReference("lessons"); // Khởi tạo lessons reference
    }

    public Completable addLessonToFolder(Lesson lesson, String folderId) {
        return Completable.create(emitter -> {
            if (folderId == null || folderId.trim().isEmpty()) {
                emitter.tryOnError(new ValidationException("Folder ID cannot be empty."));
                return;
            }
            if (lesson == null || lesson.getLessonId() == null || lesson.getLessonId().trim().isEmpty()) {
                emitter.tryOnError(new ValidationException("Lesson data is invalid or missing ID."));
                return;
            }

            lessonsReference.child(lesson.getLessonId())
                    .setValue(lesson)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DatabaseReference folderRef = foldersReference.child(folderId);

                            folderRef.runTransaction(new Transaction.Handler() {
                                @NonNull
                                @Override
                                public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                    Folder folder = mutableData.getValue(Folder.class);
                                    if (folder == null) {
                                        Log.e(TAG, "Folder not found for ID: " + folderId);
                                        return Transaction.abort();
                                    }

                                    if (folder.getLessonIds() == null) {
                                        folder.setLessonIds(new ArrayList<>());
                                    }

                                    String lessonId = lesson.getLessonId();

                                    if (!folder.getLessonIds().contains(lessonId)) {
                                        folder.getLessonIds().add(lessonId);

                                        folder.setLessonCount(folder.getLessonIds().size());
                                    } else {
                                        emitter.onComplete();
                                        return Transaction.abort();
                                    }

                                    mutableData.setValue(folder);
                                    return Transaction.success(mutableData);
                                }

                                @Override
                                public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot currentData) {
                                    if (emitter.isDisposed()) return;

                                    if (databaseError != null) {
                                        Log.e(TAG, "Transaction failed: " + databaseError.getMessage(), databaseError.toException());
                                        emitter.tryOnError(databaseError.toException());
                                    } else if (committed) {
                                        Log.d(TAG, "Lesson added to folder successfully: " + folderId);
                                        emitter.onComplete();
                                    } else {
                                        if (!emitter.isDisposed()) {
                                            emitter.onComplete();
                                        }
                                    }
                                }
                            });
                        } else {
                            Log.e(TAG, "Failed to save lesson: " + task.getException().getMessage(), task.getException());
                            emitter.tryOnError(task.getException());
                        }
                    });
        });
    }

    public Completable createNewFolder(String folderName) {
        try {
            if (folderName == null || folderName.trim().isEmpty()) {
                throw new ValidationException("Folder name cannot be empty");
            }
        } catch (ValidationException e) {
            return Completable.error(e);
        }

        return Completable.create(emitter -> {
            String userId = getCurrentUserId();
            String userName = getCurrentUserName();

            if (userId == null || userName == null) {
                emitter.tryOnError(new IllegalStateException("User not logged in or name unavailable. Cannot create folder."));
                return;
            }

            String folderId = foldersReference.push().getKey();

            if (folderId == null) {
                emitter.tryOnError(new Exception("Failed to generate unique folder ID."));
                return;
            }

            Folder newFolder = new Folder(folderName, userId, userName, new Date());
            newFolder.setFolderId(folderId);
            newFolder.setLessonIds(new ArrayList<>());
            newFolder.setLessonCount(0);


            foldersReference.child(folderId).setValue(newFolder)
                    .addOnSuccessListener(aVoid -> {
                        if (emitter.isDisposed()) return;
                        emitter.onComplete();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to create folder: " + e.getMessage(), e);
                        emitter.tryOnError(e);
                    });
        });
    }

    public Single<List<Folder>> getFolders() {
        return Single.create(emitter -> {

            String userId = getCurrentUserId();
            if (userId == null) {
                emitter.tryOnError(new IllegalStateException("No user logged in or user ID unavailable"));
                return;
            }

            Query userFoldersQuery = foldersReference.orderByChild("userId").equalTo(userId);

            userFoldersQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (emitter.isDisposed()) return;

                    List<Folder> folders = new ArrayList<>();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Folder folder = snapshot.getValue(Folder.class);
                        if (folder != null) {
                            if (folder.getLessonIds() != null) {
                                folder.setLessonCount(folder.getLessonIds().size());
                            } else {
                                folder.setLessonCount(0);
                            }
                            folders.add(folder);
                        }
                    }
                    emitter.onSuccess(folders);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    if (emitter.isDisposed()) return;
                    emitter.tryOnError(databaseError.toException());
                }
            });
        });
    }

    private String getCurrentUserId() {
        return firebaseAuth.getCurrentUser() != null ? firebaseAuth.getCurrentUser().getUid() : null;
    }

    private String getCurrentUserName() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                return user.getDisplayName();
            }
            if (user.getEmail() != null) {
                return user.getEmail().split("@")[0];
            }
            return "User_" + user.getUid().substring(0, 5);
        }
        return null;
    }
}