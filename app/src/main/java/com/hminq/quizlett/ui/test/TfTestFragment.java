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
import java.util.Random;

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
    private final List<String> displayedAnswers = new ArrayList<>();
    private final List<Boolean> answerTruthValues = new ArrayList<>();

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
            testQuestions = new ArrayList<>(allQuestions.subList(0, questionCount));
        } else {
            testQuestions = allQuestions;
        }

        buildStatementsForQuestions();
    }

    private void buildStatementsForQuestions() {
        displayedAnswers.clear();
        answerTruthValues.clear();
        Random random = new Random();

        for (Question question : testQuestions) {
            String correctAnswer = resolveCorrectAnswer(question);
            String answerToDisplay = correctAnswer;
            boolean isStatementTrue = true;

            List<String> options = question.getAnswerOptions();
            if (options != null && options.size() > 1) {
                List<String> wrongAnswers = new ArrayList<>();
                for (int i = 0; i < options.size(); i++) {
                    if (i == question.getCorrectAnswerIndex()) {
                        continue;
                    }
                    String option = options.get(i);
                    if (option != null && !option.trim().isEmpty()) {
                        wrongAnswers.add(option);
                    }
                }

                if (!wrongAnswers.isEmpty()) {
                    isStatementTrue = random.nextBoolean();
                    if (!isStatementTrue) {
                        answerToDisplay = wrongAnswers.get(random.nextInt(wrongAnswers.size()));
                    }
                }
            }

            displayedAnswers.add(answerToDisplay);
            answerTruthValues.add(isStatementTrue);
        }
    }

    private String resolveCorrectAnswer(Question question) {
        List<String> options = question.getAnswerOptions();
        int correctIndex = question.getCorrectAnswerIndex();
        if (options != null && correctIndex >= 0 && correctIndex < options.size()) {
            return options.get(correctIndex);
        }
        return "";
    }

    private void displayCurrentQuestion() {
        if (currentQuestionIndex < testQuestions.size()) {
            Question question = testQuestions.get(currentQuestionIndex);
            binding.tvQuestion.setText(question.getQuestionText());
            binding.tvQuestionProgress.setText((currentQuestionIndex + 1) + "/" + testQuestions.size());
            binding.questionProgressBar.setMax(testQuestions.size());
            binding.questionProgressBar.setProgress(currentQuestionIndex + 1);
            if (currentQuestionIndex < displayedAnswers.size()) {
                binding.tvAnswerHint.setText(getString(R.string.tf_answer_hint, displayedAnswers.get(currentQuestionIndex)));
            }
        } else {
            finishTest();
        }
    }

    private void setupListeners() {
        binding.btnClose.setOnClickListener(v -> navController.popBackStack());

        binding.btnTrue.setOnClickListener(v -> handleAnswer(true));
        binding.btnFalse.setOnClickListener(v -> handleAnswer(false));
    }

    private void handleAnswer(boolean isTrueSelected) {
        Question currentQuestion = testQuestions.get(currentQuestionIndex);
        boolean isStatementTrue = answerTruthValues.get(currentQuestionIndex);
        String displayedAnswer = displayedAnswers.get(currentQuestionIndex);
        String correctAnswer = resolveCorrectAnswer(currentQuestion);

        if (isTrueSelected == isStatementTrue) {
            correctAnswers++;
        } else {
            incorrectAnswers++;
            incorrectAnswerList.add(new IncorrectAnswer(
                    currentQuestion.getQuestionText(),
                    formatSelectionLabel(isTrueSelected, displayedAnswer, correctAnswer),
                    formatSelectionLabel(isStatementTrue, displayedAnswer, correctAnswer)));
        }

        currentQuestionIndex++;
        displayCurrentQuestion();
    }

    private String formatSelectionLabel(boolean isTrueSelection, String displayedAnswer, String correctAnswer) {
        if (isTrueSelection) {
            return getString(R.string.tf_truth_label_true, displayedAnswer);
        } else {
            return getString(R.string.tf_truth_label_false, correctAnswer);
        }
    }

    private void finishTest() {
        Bundle bundle = new Bundle();
        bundle.putInt("correctAnswers", correctAnswers);
        bundle.putInt("incorrectAnswers", incorrectAnswers);
        bundle.putSerializable("incorrectAnswerList", incorrectAnswerList);
        bundle.putSerializable("lesson", lesson);
        navController.navigate(R.id.action_tfTestFragment_to_resultFragment, bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}


