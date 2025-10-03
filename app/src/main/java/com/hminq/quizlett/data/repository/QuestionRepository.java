package com.hminq.quizlett.data.repository;

import android.util.Log;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hminq.quizlett.data.remote.model.Difficulty;
import com.hminq.quizlett.data.remote.model.Question;
import com.hminq.quizlett.data.dto.request.AddQuestionRequest;
import com.hminq.quizlett.data.dto.request.UpdateQuestionRequest;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

public class QuestionRepository {
    private static final String TAG = "QUESTION_REPO";
    private DatabaseReference questionReference;

    @Inject
    public QuestionRepository(FirebaseDatabase firebaseDatabase) {
        Log.d(TAG, "QuestionRepository initialized");
        this.questionReference = firebaseDatabase.getReference("questions");
    }

    // Added userId parameter
    public void addQuestion(AddQuestionRequest request, String userId, OnQuestionAddedListener listener) {
        Log.d(TAG, "addQuestion called with request: " + request.getQuestionText() + " for userId: " + userId);
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "User ID is null or empty for addQuestion");
            listener.onQuestionAdded(false, "User ID cannot be null or empty.");
            return;
        }
        String quesId = questionReference.push().getKey();
        if (quesId != null) {
            Log.d(TAG, "Generated quesId: " + quesId);
            // Added userId to Question constructor
            Question question = new Question(quesId, request.getQuestionText(), request.getAnswerOptions(), request.getCorrectAnswerIndex(), request.getDifficulty(), userId);
            questionReference.child(quesId).setValue(question)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "addQuestion successful for quesId: " + quesId);
                        listener.onQuestionAdded(true, null);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "addQuestion failed for quesId: " + quesId, e);
                        listener.onQuestionAdded(false, e.getMessage());
                    });
        } else {
            Log.e(TAG, "Could not generate question ID for addQuestion");
            listener.onQuestionAdded(false, "Could not generate question ID.");
        }
    }

    // Added userId parameter
    public void updateQuestion(UpdateQuestionRequest request, String userId, OnQuestionUpdatedListener listener) {
        Log.d(TAG, "updateQuestion called for quesId: " + request.getQuesId() + " by userId: " + userId);
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "User ID is null or empty for updateQuestion");
            listener.onQuestionUpdated(false, "User ID cannot be null or empty.");
            return;
        }
        if (request.getQuesId() != null) {
            // Added userId to Question constructor
            Question question = new Question(request.getQuesId(), request.getQuestionText(), request.getAnswerOptions(), request.getCorrectAnswerIndex(), request.getDifficulty(), userId);
            // TODO: Optional - Add a check here to ensure the userId matches the original creator if only creators can update.
            // For now, it updates the question with the passed userId.
            questionReference.child(request.getQuesId()).setValue(question)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "updateQuestion successful for quesId: " + request.getQuesId());
                        listener.onQuestionUpdated(true, null);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "updateQuestion failed for quesId: " + request.getQuesId(), e);
                        listener.onQuestionUpdated(false, e.getMessage());
                    });
        } else {
            Log.e(TAG, "Question ID is null for updateQuestion");
            listener.onQuestionUpdated(false, "Question ID cannot be null for update.");
        }
    }

    public void deleteQuestion(String quesId, OnQuestionDeletedListener listener) {
        Log.d(TAG, "deleteQuestion called for quesId: " + quesId);
        questionReference.child(quesId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "deleteQuestion successful for quesId: " + quesId);
                    listener.onQuestionDeleted(true, null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "deleteQuestion failed for quesId: " + quesId, e);
                    listener.onQuestionDeleted(false, e.getMessage());
                });
    }

    public void getQuestionById(String quesId, OnQuestionLoadedListener listener) {
        Log.d(TAG, "getQuestionById called for quesId: " + quesId);
        questionReference.child(quesId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Question question = dataSnapshot.getValue(Question.class);
                if (question != null) {
                    Log.d(TAG, "getQuestionById successful for quesId: " + quesId);
                    listener.onQuestionLoaded(question, null);
                } else {
                    Log.d(TAG, "getQuestionById: No data found for quesId: " + quesId);
                    listener.onQuestionLoaded(null, "Question not found.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "getQuestionById onCancelled for quesId: " + quesId, databaseError.toException());
                listener.onQuestionLoaded(null, databaseError.getMessage());
            }
        });
    }

    public void getAllQuestions(String userId, OnQuestionsLoadedListener listener) {
        Log.d(TAG, "getAllQuestions called for userId: " + userId);
        questionReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Question> questions = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Question question = snapshot.getValue(Question.class);
                    if (question != null && userId.equals(question.getUserId())) {
                        questions.add(question);
                    }
                }
                Log.d(TAG, "getAllQuestions successful, loaded " + questions.size() + " questions for userId " + userId);
                listener.onQuestionsLoaded(questions, null);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "getAllQuestions onCancelled for userId " + userId, databaseError.toException());
                listener.onQuestionsLoaded(null, databaseError.getMessage());
            }
        });
    }

    public void getQuestionsByDifficulty(Difficulty difficulty, String userId, OnQuestionsLoadedListener listener) {
        Log.d(TAG, "getQuestionsByDifficulty called for difficulty: " + difficulty.name() + " and userId: " + userId);
        questionReference.orderByChild("difficulty").equalTo(difficulty.name())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<Question> questions = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Question question = snapshot.getValue(Question.class);
                            if (question != null && userId.equals(question.getUserId())) {
                                questions.add(question);
                            }
                        }
                        Log.d(TAG, "getQuestionsByDifficulty successful for " + difficulty.name()
                                + " and userId " + userId + ", loaded " + questions.size() + " questions.");
                        listener.onQuestionsLoaded(questions, null);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "getQuestionsByDifficulty onCancelled for difficulty: "
                                + difficulty.name() + " and userId " + userId, databaseError.toException());
                        listener.onQuestionsLoaded(null, databaseError.getMessage());
                    }
                });
    }


    public interface OnQuestionAddedListener {
        void onQuestionAdded(boolean success, String errorMessage);
    }

    public interface OnQuestionUpdatedListener {
        void onQuestionUpdated(boolean success, String errorMessage);
    }

    public interface OnQuestionDeletedListener {
        void onQuestionDeleted(boolean success, String errorMessage);
    }

    public interface OnQuestionLoadedListener {
        void onQuestionLoaded(Question question, String errorMessage);
    }

    public interface OnQuestionsLoadedListener {
        void onQuestionsLoaded(List<Question> questions, String errorMessage);
    }
}
