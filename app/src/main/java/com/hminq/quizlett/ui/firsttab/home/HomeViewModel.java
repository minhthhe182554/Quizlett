package com.hminq.quizlett.ui.firsttab.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hminq.quizlett.data.remote.model.Lesson;
import com.hminq.quizlett.data.remote.model.LessonCategory;
import com.hminq.quizlett.data.repository.LessonRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class HomeViewModel extends ViewModel {

    private final LessonRepository lessonRepository;

    private final MutableLiveData<List<Lesson>> _allLessons = new MutableLiveData<>();
    private final MutableLiveData<Map<LessonCategory, List<Lesson>>> _lessonsByCategory = new MutableLiveData<>();
    private final MutableLiveData<List<Lesson>> _searchResults = new MutableLiveData<>();
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);

    public LiveData<List<Lesson>> getAllLessons() { return _allLessons; }
    public LiveData<Map<LessonCategory, List<Lesson>>> getLessonsByCategory() { return _lessonsByCategory; }
    public LiveData<List<Lesson>> getSearchResults() { return _searchResults; }
    public LiveData<String> getErrorMessage() { return _errorMessage; }
    public LiveData<Boolean> getIsLoading() { return _isLoading; }

    @Inject
    public HomeViewModel(LessonRepository lessonRepository) {
        this.lessonRepository = lessonRepository;
    }

    /**
     * Load all lessons (without user filter)
     * Use this if you want to show all lessons in the system
     */
    public void loadAllLessons() {
        _isLoading.setValue(true);

        lessonRepository.getAllLessons((lessons, error) -> {
            _isLoading.setValue(false);

            if (error != null) {
                _errorMessage.setValue(error);
            } else {
                _allLessons.setValue(lessons != null ? lessons : new ArrayList<>());
                categorizeLessons(lessons);
            }
        });
    }

    /**
     * Load lessons for a specific user
     * @param userId The user ID to filter lessons
     */
    public void loadLessonsByUser(String userId) {
        _isLoading.setValue(true);

        lessonRepository.getAllLessonsByUser(userId, (lessons, error) -> {
            _isLoading.setValue(false);

            if (error != null) {
                _errorMessage.setValue(error);
            } else {
                _allLessons.setValue(lessons != null ? lessons : new ArrayList<>());
                categorizeLessons(lessons);
            }
        });
    }

    private void categorizeLessons(List<Lesson> lessons) {
        if (lessons == null || lessons.isEmpty()) {
            _lessonsByCategory.setValue(new HashMap<>());
            return;
        }

        Map<LessonCategory, List<Lesson>> categoryMap = new HashMap<>();

        for (Lesson lesson : lessons) {
            LessonCategory category = lesson.getCategory();
            if (category == null) {
                category = LessonCategory.Others;
            }

            if (!categoryMap.containsKey(category)) {
                categoryMap.put(category, new ArrayList<>());
            }
            categoryMap.get(category).add(lesson);
        }

        _lessonsByCategory.setValue(categoryMap);
    }

    public void searchLessons(String query) {
        if (query == null || query.trim().isEmpty()) {
            _searchResults.setValue(new ArrayList<>());
            return;
        }

        List<Lesson> allLessons = _allLessons.getValue();
        if (allLessons == null || allLessons.isEmpty()) {
            _searchResults.setValue(new ArrayList<>());
            return;
        }

        String lowerQuery = query.toLowerCase().trim();
        List<Lesson> results = new ArrayList<>();

        for (Lesson lesson : allLessons) {
            if (lesson.getTitle() != null &&
                    lesson.getTitle().toLowerCase().contains(lowerQuery)) {
                results.add(lesson);
            }
        }

        _searchResults.setValue(results);
    }

    public void updateVisitCount(String lessonId) {
        lessonRepository.updateVisitCount(lessonId);
    }

    public void clearError() {
        _errorMessage.setValue(null);
    }
}