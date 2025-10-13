package com.hminq.quizlett.ui.secondtab.lesson.list;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hminq.quizlett.data.remote.model.Lesson;
import com.hminq.quizlett.data.remote.model.LessonCategory;
import com.hminq.quizlett.data.repository.LessonRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LessonListViewModel extends ViewModel {

    private final LessonRepository lessonRepository;

    private final MutableLiveData<List<Lesson>> _filteredLessons = new MutableLiveData<>();
    public LiveData<List<Lesson>> getFilteredLessons() { return _filteredLessons; }

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> getErrorMessage() { return _errorMessage; }

    private List<Lesson> allLessons = new ArrayList<>();
    private String searchQuery = "";
    private LessonCategory categoryFilter = null;

    @Inject
    public LessonListViewModel(LessonRepository lessonRepository) {
        this.lessonRepository = lessonRepository;
    }

    public void loadLessons(String userId) {
        if (userId == null || userId.isEmpty()) {
            _errorMessage.setValue("No user logged in");
            _filteredLessons.setValue(new ArrayList<>());
            return;
        }

        lessonRepository.getAllLessonsByUser(userId, (lessons, error) -> {
            if (error != null) {
                _errorMessage.setValue(error);
                allLessons = new ArrayList<>();
            } else {
                allLessons = lessons != null ? lessons : new ArrayList<>();
            }
            applyFilters();
        });
    }

    public void setSearchQuery(String query) {
        searchQuery = query != null ? query.toLowerCase().trim() : "";
        applyFilters();
    }

    public void setCategoryFilter(LessonCategory category) {
        categoryFilter = category;
        applyFilters();
    }

    private void applyFilters() {
        List<Lesson> filtered = new ArrayList<>(allLessons);

        // Filter by category
        if (categoryFilter != null) {
            filtered = filtered.stream()
                    .filter(l -> l.getCategory() == categoryFilter)
                    .collect(Collectors.toList());
        }

        // Filter by search query (title)
        if (!searchQuery.isEmpty()) {
            filtered = filtered.stream()
                    .filter(l -> l.getTitle() != null && l.getTitle().toLowerCase().contains(searchQuery))
                    .collect(Collectors.toList());
        }


        filtered.sort((l1, l2) -> {
            if (l1.getLastVisited() == null && l2.getLastVisited() == null) return 0;
            if (l1.getLastVisited() == null) return 1;
            if (l2.getLastVisited() == null) return -1;
            return l2.getLastVisited().compareTo(l1.getLastVisited());
        });

        _filteredLessons.setValue(filtered);
    }
}
