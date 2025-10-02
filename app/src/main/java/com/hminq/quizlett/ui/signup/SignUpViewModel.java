package com.hminq.quizlett.ui.signup;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hminq.quizlett.data.repository.UserRepository;
import com.hminq.quizlett.exceptions.ValidationException;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class SignUpViewModel extends ViewModel {
    private static final String TAG = "SIGNUP_VIEWMODEL";
    private final MutableLiveData<Throwable> signUpError = new MutableLiveData<>();
    private final MutableLiveData<ValidationException> validationException = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isUserSignedUp = new MutableLiveData<>(false);
    private final UserRepository userRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    public SignUpViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void signUp(String email, String password, String fullname) {
        disposables.add(
                userRepository.signUp(email, password, fullname)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(authResult -> {
                            isUserSignedUp.postValue(true);
                        }, throwable -> {
                            if (throwable instanceof ValidationException) {
                                validationException.postValue((ValidationException) throwable);
                            } else {
                                // Firebase error
                                signUpError.postValue(throwable);
                            }
                        })
        );
    }

    public LiveData<Throwable> getSignUpErrorLiveData() {
        return this.signUpError;
    }

    public LiveData<ValidationException> getValidationExceptionLiveData() {
        return this.validationException;
    }

    public LiveData<Boolean> getIsUserSignedUpLiveData() {
        return this.isUserSignedUp;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
