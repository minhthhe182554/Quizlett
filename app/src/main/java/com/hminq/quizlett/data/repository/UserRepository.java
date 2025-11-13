package com.hminq.quizlett.data.repository;

import static com.hminq.quizlett.constants.UserConstant.ERROR_IMG_URL;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.bumptech.glide.load.engine.Resource;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.hminq.quizlett.data.remote.model.User;
import com.hminq.quizlett.data.remote.model.Language; // Import Language Enum
import com.hminq.quizlett.exceptions.ValidationException;
import com.hminq.quizlett.utils.InputValidator;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class UserRepository {
    private static final String TAG = "USERREPOSITORY";
    private final FirebaseAuth firebaseAuth;
    private final DatabaseReference userReference;
    private final StorageReference profileImageReference;
    private final MutableLiveData<Resource<User>> currentUserResource = new MutableLiveData<>();


    @Inject
    public UserRepository(FirebaseAuth firebaseAuth,
                          FirebaseDatabase firebaseDatabase,
                          StorageReference profileImageReference) {
        this.firebaseAuth = firebaseAuth;
        this.userReference = firebaseDatabase.getReference("users");
        this.profileImageReference = profileImageReference;

    }


    public Single<User> getCurrentUser() {
        return Single.create(emitter -> {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

            if (firebaseUser == null) {
                emitter.tryOnError(new Exception("No user logged in"));

                return;
            }

            userReference.child(firebaseUser.getUid())
                    .get()
                    .addOnSuccessListener(dataSnapshot -> {
                        if (emitter.isDisposed()) return;

                        User currentUser = dataSnapshot.getValue(User.class);

                        if (currentUser == null) {
                            emitter.tryOnError(new Exception("User not found in DB"));
                        }
                        else {
                            emitter.onSuccess(currentUser);
                        }
                    })
                    .addOnFailureListener(emitter::tryOnError);
        });
    }

    public Single<AuthResult> signIn(String email, String password) {

        try {
            InputValidator.validateInput(email, password);
        } catch (ValidationException e) {
            return Single.error(e);
        }

        return Single.create(emitter ->
                firebaseAuth
                        .signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener(authResult -> {
                            if (emitter.isDisposed()) return;

                            Log.d(TAG, "sign in successful");
                            emitter.onSuccess(authResult);
                        })
                        .addOnFailureListener(emitter::tryOnError)
        );
    }

    public Single<AuthResult> signUp(String email, String password, String fullname) {

        try {
            InputValidator.validateInput(email, password, fullname);
        } catch (ValidationException e) {
            return Single.error(e);
        }

        return Single.create(
                emitter ->
                        firebaseAuth
                                .createUserWithEmailAndPassword(email, password)
                                .addOnSuccessListener(
                                        authResult -> {
                                            String newUid = authResult.getUser().getUid();

                                            User newUser = new User(newUid, email, password, fullname);
                                            Log.d(TAG, "Create new user: " + newUser);

                                            userReference.child(newUid)
                                                    .setValue(newUser)
                                                    .addOnSuccessListener(unused -> {
                                                        if (emitter.isDisposed()) return;

                                                        emitter.onSuccess(authResult);
                                                    })
                                                    .addOnFailureListener(emitter::tryOnError);
                                        }
                                )
                                .addOnFailureListener(emitter::tryOnError)
        );
    }

    public Completable resetPassword(String email) {
        try {
            InputValidator.validateInput(email);
        } catch (ValidationException e) {
            return Completable.error(e);
        }

        return Completable.create(emitter -> {
            firebaseAuth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(aVoid -> {
                        if (emitter.isDisposed()) return;

                        emitter.onComplete();
                    })
                    .addOnFailureListener(emitter::tryOnError);
        });
    }

    public Completable signOut() {
        return Completable.create(emitter -> {
            firebaseAuth.signOut();

            if (emitter.isDisposed()) return;

            emitter.onComplete();
        });
    }

    public Completable deleteAccount() {
        return Completable.create(emitter -> {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser == null) {
                emitter.tryOnError(new IllegalStateException("No user logged in"));
                return;
            }

            String uid = firebaseUser.getUid();

            firebaseUser.delete()
                    .addOnSuccessListener(unused -> {
                        Log.d(TAG, "User account deleted from Auth");

                        userReference.child(uid).removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "User data deleted from Database");

                                    deleteProfileImage(uid);

                                    if (emitter.isDisposed()) return;
                                    emitter.onComplete();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Database deletion failed: " + e.getMessage());
                                    if (emitter.isDisposed()) return;
                                    emitter.onComplete();
                                });
                    })
                    .addOnFailureListener(authError -> {
                        Log.e(TAG, "Auth deletion failed: " + authError.getMessage());

                        emitter.tryOnError(authError);
                    });
        });
    }

    private void deleteProfileImage(String uid) {
        profileImageReference.child(uid).delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Profile image deleted from Storage"))
                .addOnFailureListener(e -> Log.w(TAG, "Profile image deletion failed or doesn't exist: " + e.getMessage()));
    }

    public Completable reauthenticate(String password) {
        return Completable.create(emitter -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null || user.getEmail() == null) {
                emitter.tryOnError(new IllegalStateException("No user logged in"));
                return;
            }

            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);

            user.reauthenticate(credential)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Re-authentication successful");
                        if (emitter.isDisposed()) return;

                        emitter.onComplete();
                    })
                    .addOnFailureListener(emitter::tryOnError);
        });
    }

    public Single<String> loadProfileImage(User user) {

        return Single.create(
                emitter -> {
                    // Cập nhật: Kiểm tra nếu profileImageUrl là null hoặc rỗng
                    if (user.getProfileImageUrl() == null || user.getProfileImageUrl().isEmpty()) {
                        // Tải ảnh lỗi (hoặc ảnh mặc định) nếu không có đường dẫn ảnh hồ sơ
                        StorageReference errorImageRef = profileImageReference.child(ERROR_IMG_URL);
                        errorImageRef.getDownloadUrl()
                                .addOnSuccessListener(defaultUri -> {
                                    if (emitter.isDisposed()) return;
                                    emitter.onSuccess(defaultUri.toString());
                                })
                                .addOnFailureListener(emitter::tryOnError);
                        return;
                    }

                    StorageReference imageRef = profileImageReference.child(user.getProfileImageUrl());

                    imageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> emitter.onSuccess(uri.toString()))
                            .addOnFailureListener(e -> {
                                // fallback to error picture
                                StorageReference errorImageRef = profileImageReference.child(ERROR_IMG_URL);

                                errorImageRef.getDownloadUrl()
                                        .addOnSuccessListener(defaultUri -> {
                                            if (emitter.isDisposed()) return;

                                            emitter.onSuccess(defaultUri.toString());
                                        })
                                        .addOnFailureListener(emitter::tryOnError);
                            });
                }
        );
    }

    public Completable uploadNewProfileImage(Uri imageUri, String previousPath) {
        return Completable.create(emitter -> {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser == null) {
                emitter.tryOnError(new IllegalStateException("No user logged in"));
                return;
            }

            String uid = firebaseUser.getUid();
            String storagePath = buildProfileImagePath(uid, imageUri);
            StorageReference photoRef = profileImageReference.child(storagePath);

            photoRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        userReference.child(uid)
                                .child("profileImageUrl")
                                .setValue(storagePath)
                                .addOnSuccessListener(aVoid -> {
                                    deletePreviousProfileImage(previousPath);
                                    if (!emitter.isDisposed()) {
                                        emitter.onComplete();
                                    }
                                })
                                .addOnFailureListener(error -> {
                                    if (!emitter.isDisposed()) {
                                        emitter.tryOnError(error);
                                    }
                                });
                    })
                    .addOnFailureListener(error -> {
                        if (!emitter.isDisposed()) {
                            emitter.tryOnError(error);
                        }
                    });
        });
    }

    private String buildProfileImagePath(String uid, Uri imageUri) {
        String fileName = "profile_" + System.currentTimeMillis();
        String lastSegment = imageUri.getLastPathSegment();

        if (lastSegment != null) {
            String cleanSegment = lastSegment.substring(lastSegment.lastIndexOf('/') + 1);
            if (cleanSegment.contains(".")) {
                String extension = cleanSegment.substring(cleanSegment.lastIndexOf('.'));
                if (extension.length() <= 6) {
                    fileName += extension;
                } else {
                    fileName += ".jpg";
                }
            } else {
                fileName += ".jpg";
            }
        } else {
            fileName += ".jpg";
        }

        return uid + "/" + fileName;
    }

    private void deletePreviousProfileImage(String previousPath) {
        if (previousPath == null || previousPath.isEmpty() || previousPath.startsWith("default/")) {
            return;
        }

        profileImageReference.child(previousPath)
                .delete()
                .addOnFailureListener(e -> Log.w(TAG, "Failed to delete previous profile image: " + e.getMessage()));
    }

    /**
     * Updates the user's language setting in Firebase Realtime Database.
     * @param language The Language Enum value to set.
     * @return Completable indicating success or failure.
     */
    public Completable updateLanguageSetting(Language language) {
        return Completable.create(emitter -> {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser == null) {
                emitter.tryOnError(new IllegalStateException("No user logged in"));
                return;
            }

            // Lưu giá trị Enum dưới dạng String (VD: "ENGLISH", "VIETNAMESE")
            userReference.child(firebaseUser.getUid())
                    .child("userSetting")
                    .child("language")
                    .setValue(language.name()) // Sử dụng .name() để lưu String
                    .addOnSuccessListener(aVoid -> {
                        if (emitter.isDisposed()) return;
                        emitter.onComplete();
                    })
                    .addOnFailureListener(emitter::tryOnError);
        });
    }

    public Single<User> getUserProfile() {
        return Single.create(emitter -> {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

            if (firebaseUser == null) {
                emitter.tryOnError(new Exception("No user logged in"));
                return;
            }
            userReference.child(firebaseUser.getUid())
                    .get()
                    .addOnSuccessListener(dataSnapshot -> {
                        if (emitter.isDisposed()) return;

                        User currentUser = dataSnapshot.getValue(User.class);

                        if (currentUser == null) {
                            emitter.tryOnError(new Exception("User not found in DB"));
                        }
                        else {
                            emitter.onSuccess(currentUser);
                        }
                    })
                    .addOnFailureListener(emitter::tryOnError);
        });
    }

    public Completable updateFullname(String newFullname) {
        return Completable.create(emitter -> {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser == null) {
                emitter.tryOnError(new IllegalStateException("No user logged in"));
                return;
            }

            userReference.child(firebaseUser.getUid())
                    .child("fullname")
                    .setValue(newFullname)
                    .addOnSuccessListener(aVoid -> {
                        if (emitter.isDisposed()) return;
                        emitter.onComplete();
                    })
                    .addOnFailureListener(emitter::tryOnError);
        });
    }

    public Completable updatePassword(String newPassword, String currentPassword) {
        return reauthenticate(currentPassword)
                .andThen(Completable.create(emitter -> {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user == null) {
                        emitter.tryOnError(new IllegalStateException("No user logged in"));
                        return;
                    }

                    user.updatePassword(newPassword)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Password updated successfully");
                                if (emitter.isDisposed()) return;
                                emitter.onComplete();
                            })
                            .addOnFailureListener(emitter::tryOnError);
                }));
    }


    //fix: Get user by ID using RxJava Single (async)
    /**
     * Fetch user data by userId from Firebase Realtime Database
     * @param userId User ID to fetch
     * @return Single<User> that emits user data or error
     */
    public Single<User> getUserById(String userId) {
        return Single.create(emitter -> {
            if (userId == null || userId.isEmpty()) {
                emitter.tryOnError(new IllegalArgumentException("User ID cannot be null or empty"));
                return;
            }

            userReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (emitter.isDisposed()) return;

                    if (dataSnapshot.exists()) {
                        User foundUser = dataSnapshot.getValue(User.class);
                        if (foundUser != null) {
                            emitter.onSuccess(foundUser);
                        } else {
                            emitter.tryOnError(new Exception("User data is null for userId: " + userId));
                        }
                    } else {
                        emitter.tryOnError(new Exception("User not found with ID: " + userId));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    if (emitter.isDisposed()) return;
                    emitter.tryOnError(databaseError.toException());
                }
            });
        });
    }

    //fix: Get user profile image URL using RxJava Single (async)
    /**
     * Fetch profile image download URL for a user
     * @param userId User ID to fetch profile image for
     * @return Single<String> that emits download URL or error image URL as fallback
     */
    public Single<String> getUserProfileImageUrl(String userId) {
        if (userId == null || userId.isEmpty()) {
            return Single.error(new IllegalArgumentException("User ID cannot be null or empty"));
        }

        // First get user data to find profileImageUrl path, then load image from Storage
        return getUserById(userId)
                .flatMap(user -> {
                    // SỬA LỖI: LoadProfileImage đã được cập nhật logic kiểm tra đường dẫn, nên ta chỉ cần gọi nó
                    return loadProfileImage(user);
                });
    }


}