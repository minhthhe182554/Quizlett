package com.hminq.quizlett.data.repository;

import static com.hminq.quizlett.constants.UserConstant.ERROR_IMG_URL;

import android.util.Log;
import android.widget.ImageView;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
            if (firebaseUser == null) {
                emitter.onError(new Exception("No user logged in"));
            }
            else {
                String currentUserUid = firebaseUser.getUid();

                userReference.child(currentUserUid)
                        .get()
                        .addOnSuccessListener(dataSnapshot -> {
                            User currentUser = dataSnapshot.getValue(User.class);

                            if (currentUser == null) {
                                emitter.onError(new Exception("User not found in DB"));
                            }
                            else {
                                emitter.onSuccess(currentUser);
                            }
                        })
                        .addOnFailureListener(emitter::onError);
            }
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
                    Log.d(TAG, "sign in successful");
                    emitter.onSuccess(authResult);
                })
                .addOnFailureListener(emitter::onError)
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
                                            .addOnSuccessListener(unused -> emitter.onSuccess(authResult))
                                            .addOnFailureListener(emitter::onError);
                                }
                        )
                        .addOnFailureListener(emitter::onError)
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
                            emitter.onComplete();
                    })
                    .addOnFailureListener(emitter::onError);
        });
    }

    public Completable signOut() {
        return Completable.create(emitter -> {
            firebaseAuth.signOut();
            emitter.onComplete();
        });
    }

    public Completable deleteAccount() {
        return Completable.create(emitter -> {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser == null) {
                emitter.onError(new IllegalStateException("No user logged in"));
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

                                    emitter.onComplete();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Database deletion failed: " + e.getMessage());
                                    // Auth already deleted, but log the error
                                    // Database cleanup can be done later via Cloud Functions
                                    emitter.onComplete(); // Still complete since Auth is deleted
                                });
                    })
                    .addOnFailureListener(authError -> {
                        Log.e(TAG, "Auth deletion failed: " + authError.getMessage());
                        emitter.onError(authError); // Nothing deleted yet
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
                emitter.onError(new IllegalStateException("No user logged in"));
                return;
            }

            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);

            user.reauthenticate(credential)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Re-authentication successful");
                        emitter.onComplete();
                    })
                    .addOnFailureListener(emitter::onError);
        });
    }

    public Single<String> loadProfileImage(User user) {
        StorageReference imageRef = profileImageReference.child(user.getProfileImageUrl());

        return Single.create(
                emitter -> {
                    imageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> emitter.onSuccess(uri.toString()))
                            .addOnFailureListener(e -> {
                                // fallback to error picture
                                StorageReference errorImageRef = profileImageReference.child(ERROR_IMG_URL);

                                errorImageRef.getDownloadUrl()
                                        .addOnSuccessListener(defaultUri -> {
                                            emitter.onSuccess(defaultUri.toString());
                                        })
                                        .addOnFailureListener(emitter::onError);
                            });
                }
        );
    }


}
