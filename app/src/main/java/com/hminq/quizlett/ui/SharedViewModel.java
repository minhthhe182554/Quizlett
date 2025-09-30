package com.hminq.quizlett.ui;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hminq.quizlett.data.remote.model.User;
import com.hminq.quizlett.data.repository.UserRepository;
import com.hminq.quizlett.exceptions.ValidationException;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

@HiltViewModel
public class SharedViewModel extends ViewModel {
    private static final String TAG = "SHARED_VIEWMODEL";
    private final MutableLiveData<User> currentUser;
    private final MutableLiveData<Throwable> error = new MutableLiveData<>();
    private final UserRepository userRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    public SharedViewModel(UserRepository userRepository) {
        currentUser = new MutableLiveData<>();
        this.userRepository = userRepository;
    }

    public void getCurrentUser() {
        disposables.add(
                userRepository.getCurrentUser()
                        .subscribe(user -> {
                            currentUser.postValue(user);
                        }, throwable -> {
                            currentUser.postValue(null);
                            error.postValue(throwable);
                        })
        );
    }

    public LiveData<User> getCurrentUserLiveData() {
        return currentUser;
    }

    public LiveData<Throwable> getAuthErrorLiveData() {
        return this.error;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
