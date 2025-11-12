package com.hminq.quizlett.ui.firsttab.home;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hminq.quizlett.data.remote.model.Lesson;
import com.hminq.quizlett.data.remote.model.LessonCategory;
import com.hminq.quizlett.data.repository.LessonRepository;
import com.hminq.quizlett.data.repository.UserRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class HomeViewModel extends ViewModel {

    private static final String TAG = "HomeViewModel"; //fix
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository; //fix
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
    public HomeViewModel(LessonRepository lessonRepository, UserRepository userRepository) {
        this.lessonRepository = lessonRepository;
        this.userRepository = userRepository;
    }

    /**
     * Load all lessons (without user filter)
     * Use this if you want to show all lessons in the system
     */
    public void loadAllLessons() {
        _isLoading.setValue(true);

        lessonRepository.getAllLessons((lessons, error) -> {
            if (error != null) {
                _isLoading.setValue(false);
                _errorMessage.setValue(error);
                return;
            }

            List<Lesson> lessonList = lessons != null ? lessons : new ArrayList<>();
            
            if (lessonList.isEmpty()) {
                _isLoading.setValue(false);
                _allLessons.setValue(lessonList);
                categorizeLessons(lessonList);
                return;
            }

            //fix: Load creator images for all lessons asynchronously
            loadCreatorImagesForLessons(lessonList, () -> {
                _isLoading.setValue(false);
                _allLessons.setValue(lessonList);
                categorizeLessons(lessonList);
            });
        });
    }

    /**
     * Load lessons for a specific user
     * @param userId The user ID to filter lessons
     */
    public void loadLessonsByUser(String userId) {
        _isLoading.setValue(true);

        lessonRepository.getAllLessonsByUser(userId, (lessons, error) -> {
            if (error != null) {
                _isLoading.setValue(false);
                _errorMessage.setValue(error);
                return;
            }

            List<Lesson> lessonList = lessons != null ? lessons : new ArrayList<>();
            
            if (lessonList.isEmpty()) {
                _isLoading.setValue(false);
                _allLessons.setValue(lessonList);
                categorizeLessons(lessonList);
                return;
            }

            //fix: Load creator images for all lessons asynchronously
            loadCreatorImagesForLessons(lessonList, () -> {
                _isLoading.setValue(false);
                _allLessons.setValue(lessonList);
                categorizeLessons(lessonList);
            });
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

    //fix: Load creator profile images for each lesson asynchronously
    /**
     * Load creator images for a list of lessons
     * Uses AtomicInteger to track completion of all async image loads
     * @param lessons List of lessons to load images for
     * @param onComplete Callback when all images are loaded
     */
    private void loadCreatorImagesForLessons(List<Lesson> lessons, Runnable onComplete) {
        if (lessons == null || lessons.isEmpty()) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        AtomicInteger pendingLoads = new AtomicInteger(lessons.size());

        for (Lesson lesson : lessons) {
            loadCreatorImageForLesson(lesson, () -> {
                if (pendingLoads.decrementAndGet() == 0) {
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            });
        }
    }

    //fix: Load creator image for a single lesson using RxJava
    /**
     * Load creator profile image URL for a single lesson
     * Uses UserRepository.getUserProfileImageUrl() to fetch image URL by userId
     * @param lesson The lesson to load creator image for
     * @param onComplete Callback when image load completes (success or failure)
     */
    private void loadCreatorImageForLesson(Lesson lesson, Runnable onComplete) {
        if (lesson == null || lesson.getUserId() == null) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        String userId = lesson.getUserId();

        //fix: Use new async method getUserProfileImageUrl()
        userRepository.getUserProfileImageUrl(userId)
                .subscribe(
                        imageUrl -> {
                            //fix: Set the creator image URL to the lesson
                            lesson.setCreatorImage(imageUrl);
                            Log.d(TAG, "Loaded creator image for lesson: " + lesson.getTitle());
                            if (onComplete != null) {
                                onComplete.run();
                            }
                        },
                        error -> {
                            //fix: On error, set null or keep default
                            Log.w(TAG, "Failed to load creator image for lesson: " + lesson.getTitle() + ", error: " + error.getMessage());
                            lesson.setCreatorImage(null);
                            if (onComplete != null) {
                                onComplete.run();
                            }
                        }
                );
    }
}