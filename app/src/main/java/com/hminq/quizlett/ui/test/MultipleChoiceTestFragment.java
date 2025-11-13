package com.hminq.quizlett.ui.test;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.hminq.quizlett.databinding.FagmentMultiplechoiseBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultipleChoiceTestFragment extends Fragment {

    private FagmentMultiplechoiseBinding binding;
    private NavController navController;
    private Lesson lesson;
    private List<Question> testQuestions;
    private int questionCount;
    private int currentQuestionIndex = 0;
    private int correctAnswers = 0;
    private int incorrectAnswers = 0;
    private ArrayList<IncorrectAnswer> incorrectAnswerList = new ArrayList<>();
    private List<Button> answerButtons;

    public MultipleChoiceTestFragment() {
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
        binding = FagmentMultiplechoiseBinding.inflate(inflater, container, false);
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

        answerButtons = new ArrayList<>();
        answerButtons.add(binding.btnAnswer1);
        answerButtons.add(binding.btnAnswer2);
        answerButtons.add(binding.btnAnswer3);
        answerButtons.add(binding.btnAnswer4);

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

            List<String> options = new ArrayList<>(question.getAnswerOptions());
            Collections.shuffle(options);

            for (int i = 0; i < answerButtons.size(); i++) {
                if (i < options.size()) {
                    answerButtons.get(i).setText(options.get(i));
                    answerButtons.get(i).setVisibility(View.VISIBLE);
                } else {
                    answerButtons.get(i).setVisibility(View.GONE);
                }
            }

            binding.tvQuestionProgress.setText((currentQuestionIndex + 1) + "/" + testQuestions.size());
            binding.questionProgressBar.setMax(testQuestions.size());
            binding.questionProgressBar.setProgress(currentQuestionIndex + 1);
        } else {
            finishTest();
        }
    }

    private void setupListeners() {
        binding.btnClose.setOnClickListener(v -> navController.popBackStack());
        for (Button btn : answerButtons) {
            btn.setOnClickListener(v -> {
                handleAnswer(btn.getText().toString());
            });
        }
    }

    private void handleAnswer(String selectedAnswer) {
        Question currentQuestion = testQuestions.get(currentQuestionIndex);
        String correctAnswer = currentQuestion.getAnswerOptions().get(currentQuestion.getCorrectAnswerIndex());
        if (selectedAnswer.equals(correctAnswer)) {
            correctAnswers++;
            Toast.makeText(getContext(), "Correct!", Toast.LENGTH_SHORT).show();
        } else {
            incorrectAnswers++;
            incorrectAnswerList.add(new IncorrectAnswer(currentQuestion.getQuestionText(), selectedAnswer, correctAnswer));
            Toast.makeText(getContext(), "Wrong! The answer is " + correctAnswer, Toast.LENGTH_SHORT).show();
        }
        currentQuestionIndex++;
        displayCurrentQuestion();
    }

    private void finishTest() {
        Bundle bundle = new Bundle();
        bundle.putInt("correctAnswers", correctAnswers);
        bundle.putInt("incorrectAnswers", incorrectAnswers);
        bundle.putSerializable("incorrectAnswerList", incorrectAnswerList);
        navController.navigate(R.id.action_multipleChoiceTestFragment_to_resultFragment, bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
