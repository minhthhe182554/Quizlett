package com.hminq.quizlett.di;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class FirebaseModule {
    @Provides
    @Singleton
    public FirebaseAuth provideFirebaseAuth() {
        return FirebaseAuth.getInstance();
    }

    @Provides
    @Singleton
    public FirebaseDatabase provideFirebaseDatabase() {
        return FirebaseDatabase.getInstance();
    }

    @Provides
    @Singleton
    public FirebaseStorage provideFirebaseStorage() {
        return FirebaseStorage.getInstance();
    }

    @Provides
    @Singleton
    public StorageReference provideProfileImgRef(FirebaseStorage firebaseStorage) {
        return firebaseStorage.getReference().child("profile-image");
    }
}
