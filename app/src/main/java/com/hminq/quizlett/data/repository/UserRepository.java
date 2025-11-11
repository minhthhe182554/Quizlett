package com.hminq.quizlett.data.repository;

import static com.hminq.quizlett.constants.UserConstant.ERROR_IMG_URL;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

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

            // change to tryOnError to avoid exception when observer is cleared
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
        // validate inputs
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
        // validate inputs
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
                                    // get uid from FirebaseAuth
                                    String newUid = authResult.getUser().getUid();

                                    // map userInfo for Firebase Database
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
        // validate input
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

            // 1: Delete from Auth first
            firebaseUser.delete()
                    .addOnSuccessListener(unused -> {
                        Log.d(TAG, "User account deleted from Auth");

                        // 2: Delete from Database (cleanup)
                        userReference.child(uid).removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "User data deleted from Database");

                                    // 3: Delete profile image
                                    deleteProfileImage(uid);

                                    if (emitter.isDisposed()) return;
                                    emitter.onComplete();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Database deletion failed: " + e.getMessage());
                                    // Auth already deleted, but log the error
                                    // Database cleanup can be done later via Cloud Functions
                                    if (emitter.isDisposed()) return;
                                    emitter.onComplete(); // Still complete since Auth is deleted
                                });
                    })
                    .addOnFailureListener(authError -> {
                        Log.e(TAG, "Auth deletion failed: " + authError.getMessage());

                        emitter.tryOnError(authError); // Nothing deleted yet
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
                    if (user.getProfileImageUrl() == null) {
                        emitter.tryOnError(new Exception("Profile image path is null"));
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

    public Completable uploadNewProfileImage(Uri imageUri) {
        return Completable.create(emitter -> {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser == null) {
                emitter.tryOnError(new IllegalStateException("No user logged in"));
                return;
            }

            String uid = firebaseUser.getUid();
            StorageReference photoRef = profileImageReference.child(uid + "/profile.jpg");

            photoRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        photoRef.getDownloadUrl()
                                .addOnSuccessListener(downloadUri -> {
                                    userReference.child(uid)
                                            .child("profileImageUrl")
                                            .setValue(downloadUri.toString())
                                            .addOnSuccessListener(aVoid -> {
                                                if (emitter.isDisposed()) return;
                                                emitter.onComplete();
                                            })
                                            .addOnFailureListener(emitter::tryOnError);
                                })
                                .addOnFailureListener(emitter::tryOnError);
                    })
                    .addOnFailureListener(emitter::tryOnError);
        });
    }
    public Completable updateLanguageSetting(String languageCode) {
        return Completable.create(emitter -> {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser == null) {
                emitter.tryOnError(new IllegalStateException("No user logged in"));
                return;
            }

            userReference.child(firebaseUser.getUid())
                    .child("userSetting")
                    .child("language")
                    .setValue(languageCode)
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
                    String profilePath = user.getProfileImageUrl();
                    if (profilePath == null || profilePath.isEmpty()) {
                        // No profile image set, use userId as default path
                        profilePath = userId;
                    }

                    final String finalProfilePath = profilePath;
                    StorageReference imageRef = profileImageReference.child(finalProfilePath);

                    //fix: Load image URL from Firebase Storage with error fallback
                    return Single.create(imageEmitter -> {
                        imageRef.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    if (imageEmitter.isDisposed()) return;
                                    imageEmitter.onSuccess(uri.toString());
                                })
                                .addOnFailureListener(e -> {
                                    // Fallback to error picture
                                    Log.w(TAG, "Failed to load profile image for path: " + finalProfilePath + ", using error image");
                                    StorageReference errorImageRef = profileImageReference.child(ERROR_IMG_URL);
                                    errorImageRef.getDownloadUrl()
                                            .addOnSuccessListener(defaultUri -> {
                                                if (imageEmitter.isDisposed()) return;
                                                imageEmitter.onSuccess(defaultUri.toString());
                                            })
                                            .addOnFailureListener(imageEmitter::tryOnError);
                                });
                    });
                });
    }
}
