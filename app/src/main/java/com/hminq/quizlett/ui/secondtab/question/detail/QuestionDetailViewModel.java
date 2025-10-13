package com.hminq.quizlett.ui.secondtab.question.detail;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hminq.quizlett.data.remote.model.Question;
import com.hminq.quizlett.data.repository.QuestionRepository;
import com.hminq.quizlett.data.dto.request.AddQuestionRequest;
import com.hminq.quizlett.data.dto.request.UpdateQuestionRequest;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class QuestionDetailViewModel extends ViewModel {

    private final QuestionRepository questionRepository;

    private final MutableLiveData<Question> question = new MutableLiveData<>();
    private final MutableLiveData<Boolean> actionSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    @Inject
    public QuestionDetailViewModel(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public LiveData<Question> getQuestion() {
        return question;
    }

    public LiveData<Boolean> getActionSuccess() {
        return actionSuccess;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void getQuestionById(String quesId) {
        questionRepository.getQuestionById(quesId, new QuestionRepository.OnQuestionLoadedListener() {
            @Override
            public void onQuestionLoaded(Question loadedQuestion, String error) {
                if (error != null) {
                    errorMessage.setValue(error);
                } else {
                    question.setValue(loadedQuestion);
                }
            }
        });
    }

    public void addQuestion(AddQuestionRequest request, String userId) {
        if (userId == null) {
            errorMessage.setValue("No user logged in. Cannot add question.");
            actionSuccess.setValue(false);
            return;
        }

        questionRepository.addQuestion(request, userId, new QuestionRepository.OnQuestionAddedListener() {
            @Override
            public void onQuestionAdded(boolean success, String error) {
                if (error != null) {
                    errorMessage.setValue(error);
                } else {
                    actionSuccess.setValue(success);
                }
            }
        });
    }

    public void updateQuestion(UpdateQuestionRequest request, String userId) {
        if (userId == null) {
            errorMessage.setValue("No user logged in. Cannot update question.");
            actionSuccess.setValue(false);
            return;
        }

        questionRepository.updateQuestion(request, userId, new QuestionRepository.OnQuestionUpdatedListener() {
            @Override
            public void onQuestionUpdated(boolean success, String error) {
                if (error != null) {
                    errorMessage.setValue(error);
                } else {
                    actionSuccess.setValue(success);
                }
            }
        });
    }

    public void deleteQuestion(String quesId) {

        questionRepository.deleteQuestion(quesId, new QuestionRepository.OnQuestionDeletedListener() {
            @Override
            public void onQuestionDeleted(boolean success, String error) {
                if (error != null) {
                    errorMessage.setValue(error);
                } else {
                    actionSuccess.setValue(success);
                }
            }
        });
    }
}