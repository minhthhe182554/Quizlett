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

            String lessonId = lesson.getLessonId();
            Log.d(TAG, "Adding lesson ID " + lessonId + " to folder " + folderId);
            
            // Step 1: Verify lesson exists in database
            lessonsReference.child(lessonId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DataSnapshot lessonSnapshot = task.getResult();
                            if (!lessonSnapshot.exists()) {
                                Log.e(TAG, "❌ Lesson ID " + lessonId + " does not exist in database!");
                                emitter.tryOnError(new ValidationException("Lesson does not exist in database."));
                                return;
                            }
                            
                            Log.d(TAG, "✅ Lesson exists, loading folder data...");
                            
                            // Step 2: Get folder data, modify, then save
                            foldersReference.child(folderId).get()
                                    .addOnCompleteListener(folderTask -> {
                                        if (emitter.isDisposed()) return;
                                        
                                        if (folderTask.isSuccessful()) {
                                            DataSnapshot folderSnapshot = folderTask.getResult();
                                            if (!folderSnapshot.exists()) {
                                                Log.e(TAG, "❌ Folder ID " + folderId + " does not exist in database!");
                                                emitter.tryOnError(new ValidationException("Folder does not exist in database."));
                                                return;
                                            }
                                            
                                            Folder folder = folderSnapshot.getValue(Folder.class);
                                            if (folder == null) {
                                                Log.e(TAG, "❌ Failed to deserialize folder data!");
                                                emitter.tryOnError(new Exception("Failed to deserialize folder data"));
                                                return;
                                            }
                                            
                                            Log.d(TAG, "✅ Folder loaded, adding lesson...");
                                            
                                            // Initialize lessonIds if null
                                            if (folder.getLessonIds() == null) {
                                                folder.setLessonIds(new ArrayList<>());
                                            }
                                            
                                            // Check if lesson already in folder
                                            if (folder.getLessonIds().contains(lessonId)) {
                                                Log.d(TAG, "Lesson already in folder, skipping");
                                                emitter.onComplete();
                                                return;
                                            }
                                            
                                            // Add lesson ID to folder
                                            folder.getLessonIds().add(lessonId);
                                            folder.setLessonCount(folder.getLessonIds().size());
                                            
                                            Log.d(TAG, "Saving folder with " + folder.getLessonCount() + " lessons...");
                                            
                                            // Step 3: Save updated folder back to database
                                            foldersReference.child(folderId).setValue(folder)
                                                    .addOnSuccessListener(aVoid -> {
                                                        if (emitter.isDisposed()) return;
                                                        Log.d(TAG, "✅ Lesson added to folder successfully!");
                                                        emitter.onComplete();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        if (emitter.isDisposed()) return;
                                                        Log.e(TAG, "❌ Failed to save folder: " + e.getMessage());
                                                        emitter.tryOnError(e);
                                                    });
                                        } else {
                                            Log.e(TAG, "Failed to load folder: " + folderTask.getException().getMessage());
                                            emitter.tryOnError(folderTask.getException());
                                        }
                                    });
                        } else {
                            Log.e(TAG, "Failed to verify lesson existence: " + task.getException().getMessage());
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
    
    /**
     * Delete folder by ID
     * @param folderId Folder ID to delete
     * @return Completable
     */
    public Completable deleteFolder(String folderId) {
        return Completable.create(emitter -> {
            if (folderId == null || folderId.trim().isEmpty()) {
                emitter.tryOnError(new ValidationException("Folder ID cannot be empty."));
                return;
            }
            
            String currentUserId = getCurrentUserId();
            if (currentUserId == null) {
                emitter.tryOnError(new IllegalStateException("No user logged in."));
                return;
            }
            
            Log.d(TAG, "Attempting to delete folder: " + folderId);
            
            // First verify folder exists and belongs to current user
            foldersReference.child(folderId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DataSnapshot snapshot = task.getResult();
                        if (!snapshot.exists()) {
                            Log.e(TAG, "Folder does not exist: " + folderId);
                            emitter.tryOnError(new ValidationException("Folder does not exist."));
                            return;
                        }
                        
                        Folder folder = snapshot.getValue(Folder.class);
                        if (folder == null || !currentUserId.equals(folder.getUserId())) {
                            Log.e(TAG, "User does not own this folder");
                            emitter.tryOnError(new IllegalStateException("You don't have permission to delete this folder."));
                            return;
                        }
                        
                        // Delete folder
                        foldersReference.child(folderId).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "✅ Folder deleted successfully: " + folderId);
                                if (!emitter.isDisposed()) {
                                    emitter.onComplete();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "❌ Failed to delete folder: " + e.getMessage());
                                if (!emitter.isDisposed()) {
                                    emitter.tryOnError(e);
                                }
                            });
                    } else {
                        Log.e(TAG, "Failed to verify folder: " + task.getException().getMessage());
                        emitter.tryOnError(task.getException());
                    }
                });
        });
    }

    public Completable removeLessonFromFolder(String folderId, String lessonId) {
        return Completable.create(emitter -> {
            if (folderId == null || folderId.trim().isEmpty()) {
                emitter.tryOnError(new ValidationException("Folder ID cannot be empty."));
                return;
            }
            
            if (lessonId == null || lessonId.trim().isEmpty()) {
                emitter.tryOnError(new ValidationException("Lesson ID cannot be empty."));
                return;
            }
            
            String currentUserId = getCurrentUserId();
            if (currentUserId == null) {
                emitter.tryOnError(new IllegalStateException("No user logged in."));
                return;
            }
            
            Log.d(TAG, "Removing lesson " + lessonId + " from folder " + folderId);
            
            // Fetch folder
            foldersReference.child(folderId).get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Failed to fetch folder: " + task.getException().getMessage());
                        emitter.tryOnError(task.getException());
                        return;
                    }
                    
                    DataSnapshot snapshot = task.getResult();
                    if (!snapshot.exists()) {
                        Log.e(TAG, "Folder does not exist: " + folderId);
                        emitter.tryOnError(new ValidationException("Folder does not exist."));
                        return;
                    }
                    
                    Folder folder = snapshot.getValue(Folder.class);
                    if (folder == null) {
                        Log.e(TAG, "Failed to deserialize folder");
                        emitter.tryOnError(new IllegalStateException("Failed to load folder data."));
                        return;
                    }
                    
                    if (!currentUserId.equals(folder.getUserId())) {
                        Log.e(TAG, "User does not own this folder");
                        emitter.tryOnError(new IllegalStateException("You don't have permission to modify this folder."));
                        return;
                    }
                    
                    // Remove lesson from list
                    List<String> lessonIds = folder.getLessonIds();
                    if (lessonIds == null || !lessonIds.contains(lessonId)) {
                        Log.w(TAG, "Lesson not found in folder");
                        emitter.tryOnError(new ValidationException("Lesson not found in this folder."));
                        return;
                    }
                    
                    lessonIds.remove(lessonId);
                    folder.setLessonIds(lessonIds);
                    folder.setLessonCount(lessonIds.size());
                    
                    // Save updated folder
                    foldersReference.child(folderId).setValue(folder)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "✅ Lesson removed from folder successfully");
                            if (!emitter.isDisposed()) {
                                emitter.onComplete();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "❌ Failed to update folder: " + e.getMessage());
                            if (!emitter.isDisposed()) {
                                emitter.tryOnError(e);
                            }
                        });
                });
        });
    }
}