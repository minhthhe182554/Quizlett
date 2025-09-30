package com.hminq.quizlett.ui.signin;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hminq.quizlett.data.repository.UserRepository;
import com.hminq.quizlett.exceptions.ValidationException;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

@HiltViewModel
public class SignInViewModel extends ViewModel {
    private static final String TAG = "SIGNIN_VIEWMODEL";
    private final MutableLiveData<Throwable> signInError = new MutableLiveData<>();
    private final MutableLiveData<ValidationException> validationException = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isUserSignedIn = new MutableLiveData<>(false);
    private final UserRepository userRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    public SignInViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void signIn(String email, String password) {
        disposables.add(
                userRepository.signIn(email, password)
                        .subscribe(authResult -> {
                            isUserSignedIn.setValue(true);
                        }, throwable -> {
                            if (throwable instanceof ValidationException) {
                                validationException.postValue((ValidationException) throwable);
                            } else {
                                // Firebase error
                                signInError.postValue(throwable);
                            }
                        })
        );
    }

    public LiveData<ValidationException> getValidationExceptionLiveData() {
        return this.validationException;
    }

    public LiveData<Boolean> getIsUserSignedInLiveData() {
        return this.isUserSignedIn;
    }

    public LiveData<Throwable> getSignInErrorLiveData() {
        return this.signInError;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
