package com.hminq.quizlett.data.repository;

import android.util.Log;
import com.google.firebase.database.*;
import com.hminq.quizlett.data.dto.request.LessonRequest;
import com.hminq.quizlett.data.remote.model.Lesson;
import com.hminq.quizlett.data.remote.model.LessonCategory;
import com.hminq.quizlett.data.remote.model.Question;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Single;

public class LessonRepository {

    private static final String TAG = "LESSON_REPO";
    private final DatabaseReference lessonReference;
    private final DatabaseReference questionReference;

    @Inject
    public LessonRepository(FirebaseDatabase firebaseDatabase) {
        Log.d(TAG, "LessonRepository initialized");
        this.lessonReference = firebaseDatabase.getReference("lessons");
        this.questionReference = firebaseDatabase.getReference("questions");
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
            lesson.setLastVisited(new Date());
            lesson.setVisitCount(0);
            lesson.setQuestions(questions);

            lessonReference.child(lessonId).setValue(lesson)
                    .addOnSuccessListener(aVoid -> listener.onLessonAdded(true, null))
                    .addOnFailureListener(e -> listener.onLessonAdded(false, e.getMessage()));
        });
    }

    public void createAutoLesson(String title, LessonCategory category, String userId, int questionCount, OnLessonAddedListener listener) {
        getRandomQuestionsByCategory(category, questionCount, (questions, error) -> {
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
            lesson.setLastVisited(new Date());
            lesson.setVisitCount(0);
            lesson.setQuestions(questions);

            lessonReference.child(lessonId).setValue(lesson)
                    .addOnSuccessListener(aVoid -> listener.onLessonAdded(true, null))
                    .addOnFailureListener(e -> listener.onLessonAdded(false, e.getMessage()));
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
    
    public void getRandomQuestionsByCategory(LessonCategory category, int count, OnQuestionsLoadedListener listener) {
        questionReference.orderByChild("category").equalTo(category.name())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Question> allQuestions = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Question q = child.getValue(Question.class);
                            if (q != null) {
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
            public void onDataChange(DataSnapshot snapshot) {
                Lesson lesson = snapshot.getValue(Lesson.class);
                if (lesson != null) listener.onLessonLoaded(lesson, null);
                else listener.onLessonLoaded(null, "Lesson not found.");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                listener.onLessonLoaded(null, error.getMessage());
            }
        });
    }

    public void getAllLessonsByUser(String userId, OnLessonsLoadedListener listener) {
        lessonReference.orderByChild("userId").equalTo(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Lesson> lessons = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Lesson lesson = child.getValue(Lesson.class);
                    lessons.add(lesson);
                }
                listener.onLessonsLoaded(lessons, null);
            }

            @Override
            public void onCancelled(DatabaseError error) {
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
                    existingLesson.setLastVisited(new Date());

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
            public void onDataChange(DataSnapshot snapshot) {
                List<Lesson> lessons = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Lesson lesson = child.getValue(Lesson.class);
                    if (lesson != null && (category == null || lesson.getCategory() == category)) {
                        lessons.add(lesson);
                    }
                }
                lessons.sort((l1, l2) -> l2.getLastVisited().compareTo(l1.getLastVisited()));
                listener.onLessonsLoaded(lessons, null);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                listener.onLessonsLoaded(null, error.getMessage());
            }
        });
    }

    public Single<Integer> getTotalLessons(String uid) {
        return Single.create(emitter -> {
            // Create a query, choose all question with userId = uid
            Query query = lessonReference.orderByChild("userId").equalTo(uid);

            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // count the dataSnapshot result
                    int count = (int) dataSnapshot.getChildrenCount();

                    if (!emitter.isDisposed()) { emitter.onSuccess(count);}
                }

                @Override
                public void onCancelled(@androidx.annotation.NonNull DatabaseError databaseError) {
                    emitter.tryOnError(databaseError.toException());
                }
            };

            // execute query
            query.addListenerForSingleValueEvent(valueEventListener);

            emitter.setCancellable(() -> query.removeEventListener(valueEventListener));
        });
    }

    // Listener interfaces
    public interface OnLessonAddedListener { void onLessonAdded(boolean success, String errorMessage); }
    public interface OnLessonLoadedListener { void onLessonLoaded(Lesson lesson, String errorMessage); }
    public interface OnLessonsLoadedListener { void onLessonsLoaded(List<Lesson> lessons, String errorMessage); }
    public interface OnLessonUpdatedListener { void onLessonUpdated(boolean success, String errorMessage); }
    public interface OnLessonDeletedListener { void onLessonDeleted(boolean success, String errorMessage); }
    public interface OnQuestionsLoadedListener { void onQuestionsLoaded(List<Question> questions, String errorMessage); }
}
