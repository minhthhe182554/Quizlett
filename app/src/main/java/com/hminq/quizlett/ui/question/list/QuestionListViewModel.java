package com.hminq.quizlett.ui.question.list;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hminq.quizlett.data.remote.model.Difficulty;
import com.hminq.quizlett.data.remote.model.Question;
import com.hminq.quizlett.data.repository.QuestionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class QuestionListViewModel extends ViewModel {

    private final QuestionRepository questionRepository;
    private final MutableLiveData<List<Question>> _filteredQuestions = new MutableLiveData<>();
    public LiveData<List<Question>> getFilteredQuestions() {
        return _filteredQuestions;
    }

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> getErrorMessage() {
        return _errorMessage;
    }

    private List<Question> allQuestionsList = new ArrayList<>();
    private String currentSearchQuery = "";
    // Using null for difficulty to represent "All" or no filter by difficulty
    private Difficulty currentDifficultyFilter = null; 

    @Inject
    public QuestionListViewModel(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public void loadQuestions() {
        questionRepository.getAllQuestions(new QuestionRepository.OnQuestionsLoadedListener() {
            @Override
            public void onQuestionsLoaded(List<Question> questionList, String error) {
                if (error != null) {
                    _errorMessage.setValue(error);
                    allQuestionsList = new ArrayList<>(); // Clear existing on error
                } else {
                    allQuestionsList = new ArrayList<>(questionList != null ? questionList : new ArrayList<>());
                }
                applyFilters(); // Apply current filters to the newly loaded list
            }
        });
    }

    public void setSearchQuery(String query) {
        currentSearchQuery = query != null ? query.toLowerCase().trim() : "";
        applyFilters();
    }

    public void setDifficultyFilter(Difficulty difficulty) {
        currentDifficultyFilter = difficulty; // This can be null for "All"
        applyFilters();
    }

    private void applyFilters() {
        List<Question> tempFilteredList = new ArrayList<>(allQuestionsList);

        // Filter by difficulty
        if (currentDifficultyFilter != null) {
            tempFilteredList = tempFilteredList.stream()
                    .filter(q -> q.getDifficulty() == currentDifficultyFilter)
                    .collect(Collectors.toList());
        }

        // Filter by search query (question text)
        if (!currentSearchQuery.isEmpty()) {
            tempFilteredList = tempFilteredList.stream()
                    .filter(q -> q.getQuestionText() != null && q.getQuestionText().toLowerCase().contains(currentSearchQuery))
                    .collect(Collectors.toList());
        }

        _filteredQuestions.setValue(tempFilteredList);
    }
}