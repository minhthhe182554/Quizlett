package com.hminq.quizlett.ui;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hminq.quizlett.data.remote.model.User;
import com.hminq.quizlett.data.repository.UserRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class SharedViewModel extends ViewModel {
    private static final String TAG = "SHARED_VIEWMODEL";
    private final MutableLiveData<User> currentUser = new MutableLiveData<>(); 
    private final MutableLiveData<String> profileImageUrl = new MutableLiveData<>();
    private final MutableLiveData<Throwable> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> signOutSuccess = new MutableLiveData<>();
    private final MutableLiveData<Throwable> signOutError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> deleteSuccess = new MutableLiveData<>();
    private final MutableLiveData<Throwable> deleteError = new MutableLiveData<>();
    private final UserRepository userRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    public SharedViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void getCurrentUser() {
        disposables.add(
                userRepository.getCurrentUser()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(user -> {
                            currentUser.postValue(user);
                        }, throwable -> {
                            currentUser.postValue(null);
                            error.postValue(throwable);
                        })
        );
    }

    public void loadUserImage(User user) {
        disposables.add(
                userRepository.loadProfileImage(user)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(url -> profileImageUrl.setValue(url),
                                throwable -> error.setValue(throwable)
                        )
        );
    }

    public void signOut() {
        disposables.add(
                userRepository.signOut()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    signOutSuccess.postValue(true);
                                    currentUser.setValue(null);
                                },
                                signOutError::setValue
                        )
        );
    }

    public void deleteAccount() {
        disposables.add(
                userRepository.reauthenticate(currentUser.getValue().getPassword())
                        .andThen(userRepository.deleteAccount())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> deleteSuccess.setValue(true),
                                deleteError::setValue
                        )
        );
    }

    public String getCurrentUserUid() {
        return currentUser.getValue().getUid();
    }

    public LiveData<Throwable> getDeleteErrorLiveData() {
        return deleteError;
    }

    public LiveData<Boolean> getDeleteSuccessLiveData() {
        return deleteSuccess;
    }

    public LiveData<Throwable> getSignOutErrorLiveData() {
        return signOutError;
    }

    public LiveData<Boolean> getSignOutSuccessLiveData() {
        return signOutSuccess;
    }

    public LiveData<User> getCurrentUserLiveData() {
        return currentUser;
    }
    public LiveData<String> getProfileImgUrlLiveData() {
        return profileImageUrl;
    }
    public LiveData<Throwable> getErrorLiveData() {
        return this.error;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
