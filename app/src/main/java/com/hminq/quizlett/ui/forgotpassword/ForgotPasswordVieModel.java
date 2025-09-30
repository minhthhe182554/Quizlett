package com.hminq.quizlett.ui.forgotpassword;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hminq.quizlett.data.repository.UserRepository;
import com.hminq.quizlett.exceptions.ValidationException;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

@HiltViewModel
public class ForgotPasswordVieModel extends ViewModel {
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final MutableLiveData<Boolean> resetSuccess = new MutableLiveData<>();
    private final MutableLiveData<Throwable> resetError = new MutableLiveData<>();
    private final MutableLiveData<ValidationException> validationError = new MutableLiveData<>();
    private final UserRepository userRepository;

    @Inject
    public ForgotPasswordVieModel(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void resetPassword(String email) {
        disposables.add(
                userRepository.resetPassword(email)
                        .subscribe(
                                () -> resetSuccess.postValue(true),
                                throwable -> {
                                    if (throwable instanceof ValidationException) {
                                        validationError.postValue((ValidationException) throwable);
                                    }
                                    else {
                                        resetError.postValue(throwable);
                                    }
                                }
                        )
        );
    }

    public LiveData<Boolean> getResetSuccessLiveData() {
        return resetSuccess;
    }

    public LiveData<Throwable> getResetErrorLiveData() {
        return resetError;
    }

    public LiveData<ValidationException> getValidationErrorLiveData() {
        return validationError;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
