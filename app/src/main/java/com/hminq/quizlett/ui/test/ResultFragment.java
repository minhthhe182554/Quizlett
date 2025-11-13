package com.hminq.quizlett.ui.test;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.hminq.quizlett.R;
import com.hminq.quizlett.data.remote.model.IncorrectAnswer;
import com.hminq.quizlett.data.remote.model.Lesson;
import com.hminq.quizlett.databinding.FramentResultBinding;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;

public class ResultFragment extends Fragment {

    private FramentResultBinding binding;
    private NavController navController;
    private int correctAnswers;
    private int incorrectAnswers;
    private List<IncorrectAnswer> incorrectAnswerList;
    private Lesson lesson;

    public ResultFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            correctAnswers = getArguments().getInt("correctAnswers");
            incorrectAnswers = getArguments().getInt("incorrectAnswers");
            Serializable serializable = getArguments().getSerializable("incorrectAnswerList");
            if (serializable instanceof List) {
                incorrectAnswerList = (List<IncorrectAnswer>) serializable;
            }
            Serializable lessonData = getArguments().getSerializable("lesson");
            if (lessonData instanceof Lesson) {
                lesson = (Lesson) lessonData;
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FramentResultBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = NavHostFragment.findNavController(this);

        displayResults();
        setupListeners();
    }

    private void displayResults() {
        int totalQuestions = correctAnswers + incorrectAnswers;
        binding.resultsPieChart.setResults(correctAnswers, incorrectAnswers);

        if (totalQuestions > 0) {
            int correctPercent = Math.round((correctAnswers * 100f) / totalQuestions);
            int incorrectPercent = 100 - correctPercent;

            binding.tvCorrectCount.setText(String.format(Locale.getDefault(), "%d (%d%%)", correctAnswers, correctPercent));
            binding.tvIncorrectCount.setText(String.format(Locale.getDefault(), "%d (%d%%)", incorrectAnswers, incorrectPercent));
            binding.tvProgress.setText(String.format(Locale.getDefault(), "%d%% đúng", correctPercent));
        } else {
            binding.tvCorrectCount.setText("0");
            binding.tvIncorrectCount.setText("0");
            binding.tvProgress.setText("0 câu");
        }

        if (incorrectAnswerList != null && !incorrectAnswerList.isEmpty()) {
            binding.tvIncorrectAnswers.setVisibility(View.VISIBLE);
            binding.llIncorrectAnswersContainer.setVisibility(View.VISIBLE);
            populateIncorrectAnswers();
        } else {
            binding.tvIncorrectAnswers.setVisibility(View.GONE);
            binding.llIncorrectAnswersContainer.setVisibility(View.GONE);
        }
    }

    private void populateIncorrectAnswers() {
        binding.llIncorrectAnswersContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (IncorrectAnswer incorrect : incorrectAnswerList) {
            View card = inflater.inflate(R.layout.item_incorrect_answer, binding.llIncorrectAnswersContainer, false);

            TextView tvQuestion = card.findViewById(R.id.tvIncorrectQuestion);
            TextView tvYourAnswer = card.findViewById(R.id.tvYourAnswer);
            TextView tvCorrectAnswer = card.findViewById(R.id.tvCorrectAnswer);

            tvQuestion.setText(incorrect.getQuestionText());
            tvYourAnswer.setText(getString(R.string.result_your_answer, incorrect.getYourAnswer()));
            tvCorrectAnswer.setText(getString(R.string.result_correct_answer, incorrect.getCorrectAnswer()));

            binding.llIncorrectAnswersContainer.addView(card);
        }
    }

    private void setupListeners() {
        binding.btnClose.setOnClickListener(v -> navigateToLessonDetail());

        binding.btnBackToStudy.setOnClickListener(v -> navigateToLessonDetail());

        binding.btnNewTest.setOnClickListener(v -> {
            if (lesson != null) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("lesson", lesson);
                navController.navigate(R.id.action_resultFragment_to_testFragment, bundle);
            } else {
                navController.popBackStack(R.id.testFragment, false);
            }
        });
    }

    private void navigateToLessonDetail() {
        if (lesson != null) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("lesson", lesson);
            navController.navigate(R.id.action_resultFragment_to_lessonDetailFragment3, bundle);
        } else {
            navController.popBackStack(R.id.homeFragment, false);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
