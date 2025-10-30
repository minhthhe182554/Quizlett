package com.hminq.quizlett.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hminq.quizlett.data.remote.model.Folder;
import com.hminq.quizlett.exceptions.ValidationException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;


public class FolderRepository {

    private final DatabaseReference foldersReference;
    private final FirebaseAuth firebaseAuth;
    private static final String TAG = "FOLDER_REPO";

    @Inject
    public FolderRepository(FirebaseAuth firebaseAuth,
                            FirebaseDatabase firebaseDatabase) {
        this.firebaseAuth = firebaseAuth;
        // Tham chiếu đến node "folders"
        this.foldersReference = firebaseDatabase.getReference("folders");
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
            if (userId == null) {
                emitter.tryOnError(new IllegalStateException("User not logged in. Cannot create folder."));
                return;
            }

            String folderId = foldersReference.push().getKey();

            if (folderId == null) {
                emitter.tryOnError(new Exception("Failed to generate unique folder ID."));
                return;
            }

            Folder newFolder = new Folder(folderName, userId, new Date());
            newFolder.setFolderId(folderId);

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
}