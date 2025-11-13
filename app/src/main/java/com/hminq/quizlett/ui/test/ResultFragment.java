package com.hminq.quizlett.ui.test;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.hminq.quizlett.R;
import com.hminq.quizlett.data.remote.model.IncorrectAnswer;
import com.hminq.quizlett.databinding.FramentResultBinding;
import java.io.Serializable;
import java.util.List;

public class ResultFragment extends Fragment {

    private FramentResultBinding binding;
    private NavController navController;
    private int correctAnswers;
    private int incorrectAnswers;
    private List<IncorrectAnswer> incorrectAnswerList;

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
        binding.tvCorrectCount.setText(String.valueOf(correctAnswers));
        binding.tvIncorrectCount.setText(String.valueOf(incorrectAnswers));

        int totalQuestions = correctAnswers + incorrectAnswers;
        if (totalQuestions > 0) {
            int progress = (int) (((float) correctAnswers / totalQuestions) * 100);
            binding.resultsPieChart.setProgress(progress);
        } else {
            binding.resultsPieChart.setProgress(0);
        }
        binding.tvProgress.setText(totalQuestions + "/" + totalQuestions);

        if (incorrectAnswerList != null && !incorrectAnswerList.isEmpty()) {
            binding.tvIncorrectAnswers.setVisibility(View.VISIBLE);
            binding.llIncorrectAnswersContainer.setVisibility(View.VISIBLE);
//            populateIncorrectAnswers();
        } else {
            binding.tvIncorrectAnswers.setVisibility(View.GONE);
            binding.llIncorrectAnswersContainer.setVisibility(View.GONE);
        }
    }

//    private void populateIncorrectAnswers() {
//        binding.llIncorrectAnswersContainer.removeAllViews();
//        LayoutInflater inflater = LayoutInflater.from(getContext());
//
//        for (IncorrectAnswer incorrect : incorrectAnswerList) {
//            View incorrectAnswerView = inflater.inflate(R.layout.item_incorrect_answer, binding.llIncorrectAnswersContainer, false);
//
//            TextView tvQuestion = incorrectAnswerView.findViewById(R.id.tvIncorrectQuestion);
//            TextView tvYourAnswer = incorrectAnswerView.findViewById(R.id.tvYourAnswer);
//            TextView tvCorrectAnswer = incorrectAnswerView.findViewById(R.id.tvCorrectAnswerText);
//
//            tvQuestion.setText(incorrect.getQuestionText());
//            tvYourAnswer.setText("Your answer: " + incorrect.getYourAnswer());
//            tvCorrectAnswer.setText("Correct answer: " + incorrect.getCorrectAnswer());
//
//            binding.llIncorrectAnswersContainer.addView(incorrectAnswerView);
//        }
//    }

    private void setupListeners() {
        binding.btnClose.setOnClickListener(v -> {
            navController.navigate(R.id.action_resultFragment_to_lessonDetailFragment3);
        });

        binding.btnBackToStudy.setOnClickListener(v -> {
            navController.navigate(R.id.action_resultFragment_to_lessonDetailFragment3);
        });

        binding.btnNewTest.setOnClickListener(v -> {
            navController.navigate(R.id.action_resultFragment_to_testFragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
