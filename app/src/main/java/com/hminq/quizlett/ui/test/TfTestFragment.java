package com.hminq.quizlett.ui.test;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.hminq.quizlett.R;
import com.hminq.quizlett.data.remote.model.IncorrectAnswer;
import com.hminq.quizlett.data.remote.model.Lesson;
import com.hminq.quizlett.data.remote.model.Question;
import com.hminq.quizlett.databinding.FragmentTfBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TfTestFragment extends Fragment {

    private FragmentTfBinding binding;
    private NavController navController;
    private Lesson lesson;
    private List<Question> testQuestions;
    private int questionCount;
    private int currentQuestionIndex = 0;
    private int correctAnswers = 0;
    private int incorrectAnswers = 0;
    private ArrayList<IncorrectAnswer> incorrectAnswerList = new ArrayList<>();

    public TfTestFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            lesson = (Lesson) getArguments().getSerializable("lesson");
            questionCount = getArguments().getInt("questionCount");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTfBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = NavHostFragment.findNavController(this);

        if (lesson == null || lesson.getQuestions() == null || lesson.getQuestions().isEmpty()) {
            Toast.makeText(getContext(), "Error: No questions found", Toast.LENGTH_SHORT).show();
            navController.popBackStack();
            return;
        }

        prepareQuestions();
        setupListeners();
        displayCurrentQuestion();
    }

    private void prepareQuestions() {
        List<Question> allQuestions = new ArrayList<>(lesson.getQuestions());
        Collections.shuffle(allQuestions);
        if (questionCount > 0 && allQuestions.size() > questionCount) {
            testQuestions = allQuestions.subList(0, questionCount);
        } else {
            testQuestions = allQuestions;
        }
    }

    private void displayCurrentQuestion() {
        if (currentQuestionIndex < testQuestions.size()) {
            Question question = testQuestions.get(currentQuestionIndex);
            binding.tvQuestion.setText(question.getQuestionText());
            binding.tvQuestionProgress.setText((currentQuestionIndex + 1) + "/" + testQuestions.size());
            binding.questionProgressBar.setMax(testQuestions.size());
            binding.questionProgressBar.setProgress(currentQuestionIndex + 1);
        } else {
            finishTest();
        }
    }

    private void setupListeners() {
        binding.btnClose.setOnClickListener(v -> navController.popBackStack());

        binding.btnTrue.setOnClickListener(v -> {
            handleAnswer(true);
        });

        binding.btnFalse.setOnClickListener(v -> {
            handleAnswer(false);
        });
    }

    private void handleAnswer(boolean isTrueSelected) {
        Question currentQuestion = testQuestions.get(currentQuestionIndex);
        String correctAnswer = currentQuestion.getAnswerOptions().get(currentQuestion.getCorrectAnswerIndex());
        boolean isCorrectAnswerTrue = correctAnswer.equalsIgnoreCase("True");

        if (isTrueSelected == isCorrectAnswerTrue) {
            correctAnswers++;
            Toast.makeText(getContext(), "Correct!", Toast.LENGTH_SHORT).show();
        } else {
            incorrectAnswers++;
            incorrectAnswerList.add(new IncorrectAnswer(currentQuestion.getQuestionText(), String.valueOf(isTrueSelected), correctAnswer));
            Toast.makeText(getContext(), "Wrong! The correct answer was " + correctAnswer, Toast.LENGTH_SHORT).show();
        }

        currentQuestionIndex++;
        displayCurrentQuestion();
    }

    private void finishTest() {
        Bundle bundle = new Bundle();
        bundle.putInt("correctAnswers", correctAnswers);
        bundle.putInt("incorrectAnswers", incorrectAnswers);
        bundle.putSerializable("incorrectAnswerList", incorrectAnswerList);
        navController.navigate(R.id.action_tfTestFragment_to_resultFragment, bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
