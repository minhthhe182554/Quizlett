package com.hminq.quizlett.data.repository;

import static com.hminq.quizlett.constants.UserConstant.ERROR_IMG_URL;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.*;
import com.google.firebase.storage.StorageReference;
import com.hminq.quizlett.data.dto.request.LessonRequest;
import com.hminq.quizlett.data.remote.model.Lesson;
import com.hminq.quizlett.data.remote.model.LessonCategory;
import com.hminq.quizlett.data.remote.model.Question;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import io.reactivex.rxjava3.core.Single;

public class LessonRepository {

    private static final String TAG = "LESSON_REPO";
    private final DatabaseReference lessonReference;
    private final DatabaseReference questionReference;
    private final DatabaseReference userReference;
    private final StorageReference profileImageReference;

    @Inject
    public LessonRepository(FirebaseDatabase firebaseDatabase, StorageReference profileImageReference) { //fix
        Log.d(TAG, "LessonRepository initialized");
        this.lessonReference = firebaseDatabase.getReference("lessons");
        this.questionReference = firebaseDatabase.getReference("questions");
        this.userReference = firebaseDatabase.getReference("users");
        this.profileImageReference = profileImageReference; //fix
    }

    public void addLesson(LessonRequest request, String userId, OnLessonAddedListener listener) {
        Log.d(TAG, "addLesson (manual) called with title: " + request.getTitle() + " for userId: " + userId);
        if (userId == null || userId.isEmpty()) {
            listener.onLessonAdded(false, "User ID cannot be null or empty.");
            return;
        }
        List<String> questionIds = request.getQuestionIds();
        if (questionIds == null || questionIds.isEmpty()) {
            listener.onLessonAdded(false, "Manual creation requires at least one question.");
            return;
        }

        getQuestionsByIds(questionIds, (questions, error) -> {
            if (error != null) {
                listener.onLessonAdded(false, "Failed to load questions for the lesson.");
                return;
            }
            if (questions.size() != questionIds.size()) {
                listener.onLessonAdded(false, "Could not load all requested questions.");
                return;
            }

            String lessonId = lessonReference.push().getKey();
            if (lessonId == null) {
                listener.onLessonAdded(false, "Could not generate lesson ID.");
                return;
            }

            Lesson lesson = new Lesson();
            lesson.setLessonId(lessonId);
            lesson.setTitle(request.getTitle());
            lesson.setCategory(request.getCategory());
            lesson.setUserId(userId);
            lesson.setVisitCount(0);
            lesson.setQuestions(questions);

            //fix: Get Storage path then convert to download URL
            userReference.child(userId).child("profileImageUrl").get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    String storagePath = task.getResult().getValue(String.class);

                    // Convert path to download URL
                    StorageReference imageRef = profileImageReference.child(storagePath);
                    imageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String downloadUrl = uri.toString();
                                lesson.setCreatorImage(downloadUrl);
                                Log.d(TAG, "Got download URL: " + downloadUrl);

                                lessonReference.child(lessonId).setValue(lesson)
                                        .addOnSuccessListener(aVoid -> listener.onLessonAdded(true, null))
                                        .addOnFailureListener(e -> listener.onLessonAdded(false, e.getMessage()));
                            })
                            .addOnFailureListener(e -> {
                                Log.w(TAG, "Failed to get URL, saving without image");
                                lesson.setCreatorImage(null);
                                lessonReference.child(lessonId).setValue(lesson)
                                        .addOnSuccessListener(aVoid -> listener.onLessonAdded(true, null))
                                        .addOnFailureListener(err -> listener.onLessonAdded(false, err.getMessage()));
                            });
                } else {
                    Log.d(TAG, "No profile image path found");
                    lesson.setCreatorImage(null);
                    lessonReference.child(lessonId).setValue(lesson)
                            .addOnSuccessListener(aVoid -> listener.onLessonAdded(true, null))
                            .addOnFailureListener(e -> listener.onLessonAdded(false, e.getMessage()));
                }
            });
        });
    }

    public void createAutoLesson(String title, LessonCategory category, String userId, int questionCount, OnLessonAddedListener listener) {
        getRandomQuestionsByCategory(category,userId, questionCount, (questions, error) -> {
            if (error != null) {
                listener.onLessonAdded(false, error);
                return;
            }

            String lessonId = lessonReference.push().getKey();
            if (lessonId == null) {
                listener.onLessonAdded(false, "Could not generate lesson ID.");
                return;
            }

            Lesson lesson = new Lesson();
            lesson.setLessonId(lessonId);
            lesson.setTitle(title);
            lesson.setCategory(category);
            lesson.setUserId(userId);
            lesson.setVisitCount(0);
            lesson.setQuestions(questions);

            //Get Storage path then convert to download URL
            userReference.child(userId).child("profileImageUrl").get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    String storagePath = task.getResult().getValue(String.class);

                    // Convert path to download URL
                    StorageReference imageRef = profileImageReference.child(storagePath);
                    imageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String downloadUrl = uri.toString();
                                lesson.setCreatorImage(downloadUrl);
                                Log.d(TAG, "Got download URL: " + downloadUrl);

                                lessonReference.child(lessonId).setValue(lesson)
                                        .addOnSuccessListener(aVoid -> listener.onLessonAdded(true, null))
                                        .addOnFailureListener(e -> listener.onLessonAdded(false, e.getMessage()));
                            })
                            .addOnFailureListener(e -> {
                                Log.w(TAG, "⚠️ Failed to get URL, saving auto lesson without image");
                                lesson.setCreatorImage(null);
                                lessonReference.child(lessonId).setValue(lesson)
                                        .addOnSuccessListener(aVoid -> listener.onLessonAdded(true, null))
                                        .addOnFailureListener(err -> listener.onLessonAdded(false, err.getMessage()));
                            });
                } else {
                    Log.d(TAG, "No profile image path found for auto lesson");
                    lesson.setCreatorImage(null);
                    lessonReference.child(lessonId).setValue(lesson)
                            .addOnSuccessListener(aVoid -> listener.onLessonAdded(true, null))
                            .addOnFailureListener(e -> listener.onLessonAdded(false, e.getMessage()));
                }
            });
        });
    }

    public void getQuestionsByIds(List<String> questionIds, OnQuestionsLoadedListener listener) {
        List<Question> questions = new ArrayList<>();
        if (questionIds == null || questionIds.isEmpty()) {
            listener.onQuestionsLoaded(questions, null);
            return;
        }

        AtomicInteger count = new AtomicInteger(questionIds.size());

        for (String id : questionIds) {
            questionReference.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Question question = snapshot.getValue(Question.class);
                    if (question != null) {
                        questions.add(question);
                    }
                    if (count.decrementAndGet() == 0) {
                        listener.onQuestionsLoaded(questions, null);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    if (count.decrementAndGet() == 0) {
                        listener.onQuestionsLoaded(questions, "Could not load all questions.");
                    }
                }
            });
        }
    }

    public void getRandomQuestionsByCategory(LessonCategory category, String userId, int count, OnQuestionsLoadedListener listener) {
        questionReference.orderByChild("category").equalTo(category.name())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Question> allQuestions = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Question q = child.getValue(Question.class);
                            if (q != null &&  q.getUserId().equals(userId)) {
                                allQuestions.add(q);
                            }
                        }

                        if (allQuestions.size() < count) {
                            listener.onQuestionsLoaded(null, "Not enough questions in '" + category.name() + "' category.");
                            return;
                        }

                        Collections.shuffle(allQuestions);
                        List<Question> selectedQuestions = new ArrayList<>(allQuestions.subList(0, count));
                        listener.onQuestionsLoaded(selectedQuestions, null);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onQuestionsLoaded(null, error.getMessage());
                    }
                });
    }

    public void getLessonById(String lessonId, OnLessonLoadedListener listener) {
        lessonReference.child(lessonId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Lesson lesson = snapshot.getValue(Lesson.class);
                if (lesson != null) listener.onLessonLoaded(lesson, null);
                else listener.onLessonLoaded(null, "Lesson not found.");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onLessonLoaded(null, error.getMessage());
            }
        });
    }
    public void getAllLessons(OnLessonsLoadedListener listener) {
        lessonReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Lesson> lessons = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Lesson lesson = child.getValue(Lesson.class);
                    if (lesson != null) {
                        lessons.add(lesson);
                    }
                }
                listener.onLessonsLoaded(lessons, null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onLessonsLoaded(null, error.getMessage());
            }
        });
    }

    public void updateVisitCount(String lessonId) {
        lessonReference.child(lessonId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Lesson lesson = snapshot.getValue(Lesson.class);
                if (lesson != null) {
                    int newCount = lesson.getVisitCount() + 1;
                    lessonReference.child(lessonId).child("visitCount").setValue(newCount);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to update visit count: " + error.getMessage());
            }
        });
    }

    public void getAllLessonsByUser(String userId, OnLessonsLoadedListener listener) {
        lessonReference.orderByChild("userId").equalTo(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Lesson> lessons = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Lesson lesson = child.getValue(Lesson.class);
                    if (lesson != null) {
                        lessons.add(lesson);
                    }
                }
                listener.onLessonsLoaded(lessons, null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onLessonsLoaded(null, error.getMessage());
            }
        });
    }

    public void updateLesson(String lessonId, LessonRequest request, OnLessonUpdatedListener listener) {
        Log.d(TAG, "updateLesson called for lessonId: " + lessonId);
        List<String> questionIds = request.getQuestionIds();

        getQuestionsByIds(questionIds, (questions, error) -> {
            if (error != null) {
                listener.onLessonUpdated(false, "Failed to update questions for the lesson.");
                return;
            }

            lessonReference.child(lessonId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Lesson existingLesson = snapshot.getValue(Lesson.class);
                    if (existingLesson == null) {
                        listener.onLessonUpdated(false, "Lesson not found.");
                        return;
                    }

                    existingLesson.setTitle(request.getTitle());
                    existingLesson.setCategory(request.getCategory());
                    existingLesson.setQuestions(questions);

                    lessonReference.child(lessonId).setValue(existingLesson)
                            .addOnSuccessListener(aVoid -> listener.onLessonUpdated(true, null))
                            .addOnFailureListener(e -> listener.onLessonUpdated(false, e.getMessage()));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    listener.onLessonUpdated(false, error.getMessage());
                }
            });
        });
    }

    public void deleteLesson(String lessonId, OnLessonDeletedListener listener) {
        lessonReference.child(lessonId).removeValue()
                .addOnSuccessListener(aVoid -> listener.onLessonDeleted(true, null))
                .addOnFailureListener(e -> listener.onLessonDeleted(false, e.getMessage()));
    }

    public void filterLessonsByCategory(LessonCategory category, String userId, OnLessonsLoadedListener listener) {
        Query query = lessonReference.orderByChild("userId").equalTo(userId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Lesson> lessons = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Lesson lesson = child.getValue(Lesson.class);
                    if (lesson != null && (category == null || lesson.getCategory() == category)) {
                        lessons.add(lesson);
                    }
                }
                listener.onLessonsLoaded(lessons, null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onLessonsLoaded(null, error.getMessage());
            }
        });
    }

    public Single<Integer> getTotalLessons(String uid) {
        return Single.create(emitter -> {
            Query query = lessonReference.orderByChild("userId").equalTo(uid);

            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int count = (int) dataSnapshot.getChildrenCount();

                    if (!emitter.isDisposed()) {
                        emitter.onSuccess(count);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    if (!emitter.isDisposed()) {
                        emitter.onError(databaseError.toException());
                    }
                }
            };

            query.addListenerForSingleValueEvent(valueEventListener);

            emitter.setCancellable(() -> query.removeEventListener(valueEventListener));
        });
    }

    public Single<Integer> getTotalVisitCount(String uid) {
        return Single.create(emitter -> {
            Query query = lessonReference.orderByChild("userId").equalTo(uid);

            ValueEventListener listener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (emitter.isDisposed()) {
                        return;
                    }

                    int totalVisitCount = 0;
                    if (snapshot.exists()) {
                        for (DataSnapshot lessonSnapshot : snapshot.getChildren()) {
                            Integer count = lessonSnapshot.child("visitCount").getValue(Integer.class);
                            if (count != null) {
                                totalVisitCount += count;
                            }
                        }
                    }
                    emitter.onSuccess(totalVisitCount);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    if (!emitter.isDisposed()) {
                        emitter.onError(error.toException());
                    }
                }
            };

            query.addListenerForSingleValueEvent(listener);

            emitter.setCancellable(() -> query.removeEventListener(listener));
        });
    }

    public Single<Map<String, Float>> getCategoryPercentage(String uid) {
        return Single.create(emitter -> {
            Query query = lessonReference.orderByChild("userId").equalTo(uid);

            ValueEventListener listener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (emitter.isDisposed()) {
                        return;
                    }

                    Map<String, Integer> categoryCounts = new HashMap<>();
                    for (LessonCategory categoryEnum : LessonCategory.values()) {
                        categoryCounts.put(categoryEnum.toString(), 0);
                    }

                    long totalLessons = snapshot.getChildrenCount();

                    if (snapshot.exists()) {
                        for (DataSnapshot lessonSnapshot : snapshot.getChildren()) {
                            String categoryStr = lessonSnapshot.child("category").getValue(String.class);

                            if (categoryStr != null && categoryCounts.containsKey(categoryStr)) {
                                int currentCount = categoryCounts.get(categoryStr);
                                categoryCounts.put(categoryStr, currentCount + 1);
                            }
                        }
                    }

                    Map<String, Float> categoryPercentages = new HashMap<>();
                    if (totalLessons > 0) {
                        for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
                            float percentage = ((float) entry.getValue() / totalLessons) * 100.0f;
                            categoryPercentages.put(entry.getKey(), percentage);
                        }
                    } else {
                        for (LessonCategory categoryEnum : LessonCategory.values()) {
                            categoryPercentages.put(categoryEnum.toString(), 0.0f);
                        }
                    }

                    emitter.onSuccess(categoryPercentages);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    if (!emitter.isDisposed()) {
                        emitter.onError(error.toException());
                    }
                }
            };

            query.addListenerForSingleValueEvent(listener);
            emitter.setCancellable(() -> query.removeEventListener(listener));
        });
    }

    //fix: Load creator image download URL from Storage path
    /**
     * Convert creator image path to download URL
     * Duplicate logic from UserRepository.getUserProfileImageUrl()
     * @param creatorImagePath Storage path (e.g. "userId" or "default/default_profile_image.jpg")
     * @return Download URL or null if path is null/empty
     */
    public void loadCreatorImageUrl(String creatorImagePath, OnImageUrlLoadedListener listener) {
        if (creatorImagePath == null || creatorImagePath.isEmpty()) {
            listener.onImageUrlLoaded(null, "Creator image path is null or empty");
            return;
        }

        // If already a full URL, return directly
        if (creatorImagePath.startsWith("http")) {
            listener.onImageUrlLoaded(creatorImagePath, null);
            return;
        }

        // Convert Storage path to download URL
        StorageReference imageRef = profileImageReference.child(creatorImagePath);
        imageRef.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    Log.d(TAG, "✅ Loaded creator image URL: " + downloadUrl);
                    listener.onImageUrlLoaded(downloadUrl, null);
                })
                .addOnFailureListener(e -> {
                    // Fallback to error image
                    Log.w(TAG, "⚠️ Failed to load creator image, using error image: " + e.getMessage());
                    StorageReference errorImageRef = profileImageReference.child(ERROR_IMG_URL);
                    errorImageRef.getDownloadUrl()
                            .addOnSuccessListener(defaultUri -> {
                                listener.onImageUrlLoaded(defaultUri.toString(), null);
                            })
                            .addOnFailureListener(errorEx -> {
                                Log.e(TAG, "❌ Failed to load error image: " + errorEx.getMessage());
                                listener.onImageUrlLoaded(null, errorEx.getMessage());
                            });
                });
    }

    // Listener interfaces
    public interface OnLessonAddedListener { void onLessonAdded(boolean success, String errorMessage); }
    public interface OnLessonLoadedListener { void onLessonLoaded(Lesson lesson, String errorMessage); }
    public interface OnLessonsLoadedListener { void onLessonsLoaded(List<Lesson> lessons, String errorMessage); }
    public interface OnLessonUpdatedListener { void onLessonUpdated(boolean success, String errorMessage); }
    public interface OnLessonDeletedListener { void onLessonDeleted(boolean success, String errorMessage); }
    public interface OnQuestionsLoadedListener { void onQuestionsLoaded(List<Question> questions, String errorMessage); }
    public interface OnImageUrlLoadedListener { void onImageUrlLoaded(String imageUrl, String errorMessage); } //fix
}