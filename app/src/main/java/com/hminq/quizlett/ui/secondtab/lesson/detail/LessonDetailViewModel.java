package com.hminq.quizlett.ui.secondtab.lesson.detail;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hminq.quizlett.data.dto.request.LessonRequest;
import com.hminq.quizlett.data.remote.model.Lesson;
import com.hminq.quizlett.data.remote.model.LessonCategory;
import com.hminq.quizlett.data.remote.model.Question;
import com.hminq.quizlett.data.repository.LessonRepository;
import com.hminq.quizlett.data.repository.QuestionRepository;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LessonDetailViewModel extends ViewModel {

    private final LessonRepository lessonRepository;
    private final QuestionRepository questionRepository;

    private final MutableLiveData<Lesson> _lesson = new MutableLiveData<>();
    private final MutableLiveData<List<Question>> _questionsByCategory = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _actionSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    private final MutableLiveData<List<Question>> _displayedQuestions = new MutableLiveData<>();

    @Inject
    public LessonDetailViewModel(LessonRepository lessonRepository, QuestionRepository questionRepository) {
        this.lessonRepository = lessonRepository;
        this.questionRepository = questionRepository;
    }

    public LiveData<Lesson> getLesson() { return _lesson; }
    public LiveData<List<Question>> getQuestionsByCategory() { return _questionsByCategory; }
    public LiveData<Boolean> getActionSuccess() { return _actionSuccess; }
    public LiveData<String> getErrorMessage() { return _errorMessage; }
    public LiveData<List<Question>> getDisplayedQuestions() { return _displayedQuestions; }

    public void doneAction() {
        _actionSuccess.setValue(null);
    }

    public void doneError() {
        _errorMessage.setValue(null);
    }

    public void clearQuestionsByCategory() {
        _questionsByCategory.setValue(new ArrayList<>());
    }

    public void clearDisplayedQuestions() {
        _displayedQuestions.setValue(new ArrayList<>());
    }

    public void getLessonById(String lessonId) {
        lessonRepository.getLessonById(lessonId, (loaded, err) -> {
            if (err != null) {
                _errorMessage.setValue(err);
            } else {
                _lesson.setValue(loaded);
                if (loaded != null && loaded.getQuestions() != null) {
                    _displayedQuestions.setValue(loaded.getQuestions());
                }
            }
        });
    }

    public void loadQuestionsByIds(List<String> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            _displayedQuestions.setValue(new ArrayList<>());
            return;
        }
        lessonRepository.getQuestionsByIds(questionIds, (questions, error) -> {
            if (error != null) {
                _errorMessage.setValue("Failed to load selected questions: " + error);
            } else {
                _displayedQuestions.setValue(questions);
            }
        });
    }

    public void fetchQuestionsByCategory(LessonCategory category, String userId) {
        questionRepository.getQuestionsByCategory(category, userId, (questions, err) -> {
            if (err != null) _errorMessage.setValue(err);
            else _questionsByCategory.setValue(questions);
        });
    }

    public void addLesson(String title, LessonCategory category, String userId, List<String> questionIds) {
        LessonRequest request = new LessonRequest(null, title, category, (ArrayList<String>) questionIds, null);
        lessonRepository.addLesson(request, userId, (success, err) -> {
            if (err != null) _errorMessage.setValue(err);
            else _actionSuccess.setValue(success);
        });
    }

    public void createAutoLesson(String title, LessonCategory category, String userId, int questionCount) {
        lessonRepository.createAutoLesson(title, category, userId, questionCount, (success, err) -> {
            if (err != null) _errorMessage.setValue(err);
            else _actionSuccess.setValue(success);
        });
    }


    public void updateLesson(String id, String title, LessonCategory category, List<String> questionIds) {
        LessonRequest request = new LessonRequest(id, title, category, (ArrayList<String>) questionIds, null);
        lessonRepository.updateLesson(id, request, (success, err) -> {
            if (err != null) _errorMessage.setValue(err);
            else _actionSuccess.setValue(success);
        });
    }

    public void deleteLesson(String id) {
        lessonRepository.deleteLesson(id, (success, err) -> {
            if (err != null) _errorMessage.setValue(err);
            else _actionSuccess.setValue(success);
        });
    }
}
