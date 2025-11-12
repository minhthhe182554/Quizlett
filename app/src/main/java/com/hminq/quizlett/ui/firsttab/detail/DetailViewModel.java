package com.hminq.quizlett.ui.firsttab.detail;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hminq.quizlett.data.remote.model.User;
import com.hminq.quizlett.data.repository.LessonRepository;
import com.hminq.quizlett.data.repository.UserRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class DetailViewModel extends ViewModel {
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;

    private final MutableLiveData<User> _creator = new MutableLiveData<>();
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();

    public LiveData<User> getCreator() { return _creator; }
    public LiveData<String> getErrorMessage() { return _errorMessage; }

    @Inject
    public DetailViewModel(LessonRepository lessonRepository, UserRepository userRepository) {
        this.lessonRepository = lessonRepository;
        this.userRepository = userRepository;
    }

    /**
     * Load creator information by userId
     * @param userId The user ID of lesson creator
     */
    public void loadCreatorInfo(String userId) {
        if (userId == null || userId.isEmpty()) {
            _errorMessage.setValue("User ID is invalid");
            return;
        }

        userRepository.getUserById(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        user -> _creator.setValue(user),
                        error -> _errorMessage.setValue(error.getMessage())
                );
    }
}