package com.hminq.quizlett.data.repository;

import android.util.Log;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
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
    public UserRepository(FirebaseAuth firebaseAuth, DatabaseReference userReference, StorageReference profileImageReference) {
        this.firebaseAuth = firebaseAuth;
        this.userReference = userReference;
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
}
