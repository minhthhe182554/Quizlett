package com.hminq.quizlett.ui.secondtab.question.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.hminq.quizlett.R;
import com.hminq.quizlett.data.remote.model.Difficulty;
import com.hminq.quizlett.data.remote.model.Question;
import com.hminq.quizlett.data.dto.request.AddQuestionRequest;
import com.hminq.quizlett.data.dto.request.UpdateQuestionRequest;
import com.hminq.quizlett.databinding.FragmentQuestionDetailBinding;
import com.hminq.quizlett.ui.SharedViewModel;
import com.hminq.quizlett.utils.Message;


import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class QuestionDetailFragment extends Fragment {
    private static final String TAG = "FRAGMENT_QUESTION_DETAIL";
    private FragmentQuestionDetailBinding binding;
    private QuestionDetailViewModel viewModel;
    private SharedViewModel sharedViewModel;
    private NavController navController;
    private String quesId = null;
    private String currentUserId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        navController = NavHostFragment.findNavController(this);
        viewModel = new ViewModelProvider(this).get(QuestionDetailViewModel.class);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        currentUserId = sharedViewModel.getCurrentUserUid();
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentQuestionDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupSpinners();

        if (getArguments() != null) {
            quesId = getArguments().getString("quesId");
            if (quesId != null) {
                viewModel.getQuestionById(quesId);
                binding.btnDeleteQuestion.setVisibility(View.VISIBLE);
            } else {
                binding.btnDeleteQuestion.setVisibility(View.GONE);
            }
        } else {
            binding.btnDeleteQuestion.setVisibility(View.GONE);
        }

        observeViewModel(view);
        setupClickListeners();
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> answerAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.answer_options_array,
                R.layout.spiner_item
        );
        answerAdapter.setDropDownViewResource(R.layout.spiner_item);
        binding.spinnerCorrectAnswer.setAdapter(answerAdapter);


        ArrayAdapter<CharSequence> difficultyAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.difficulty_array,
                R.layout.spiner_item
        );
        difficultyAdapter.setDropDownViewResource(R.layout.spiner_item);
        binding.spinnerDifficulty.setAdapter(difficultyAdapter);
    }

    private void observeViewModel(View view) {
        viewModel.getQuestion().observe(getViewLifecycleOwner(), question -> {
            if (question != null) {
                populateUi(question);
            }
        });

        viewModel.getActionSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Message.showShort(view, "Operation successful!");
                navController.popBackStack();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                Message.showShort(view, errorMessage);
            }
        });
    }

    private void populateUi(Question question) {
        binding.etQuestionText.setText(question.getQuestionText());
        List<String> options = question.getAnswerOptions();
        if (options != null && options.size() == 4) {
            binding.etOption1.setText(options.get(0));
            binding.etOption2.setText(options.get(1));
            binding.etOption3.setText(options.get(2));
            binding.etOption4.setText(options.get(3));
        }


        if (question.getCorrectAnswerIndex() >= 0 && question.getCorrectAnswerIndex() < binding.spinnerCorrectAnswer.getCount()) {
            binding.spinnerCorrectAnswer.setSelection(question.getCorrectAnswerIndex());
        }


        String[] difficultyArray = getResources().getStringArray(R.array.difficulty_array);
        int difficultyPosition = Arrays.asList(difficultyArray).indexOf(question.getDifficulty().name());
        if (difficultyPosition != -1) {
            binding.spinnerDifficulty.setSelection(difficultyPosition);
        }
    }

    private void setupClickListeners() {
        binding.btnSaveQuestion.setOnClickListener(v -> saveQuestion());
        binding.btnDeleteQuestion.setOnClickListener(v -> deleteQuestion());
        binding.btnBack.setOnClickListener(v -> navController.popBackStack()); // Added/Restored back button listener
    }

    private void saveQuestion() {
        String questionText = binding.etQuestionText.getText().toString().trim();
        List<String> answerOptions = new ArrayList<>();
        answerOptions.add(binding.etOption1.getText().toString().trim());
        answerOptions.add(binding.etOption2.getText().toString().trim());
        answerOptions.add(binding.etOption3.getText().toString().trim());
        answerOptions.add(binding.etOption4.getText().toString().trim());
        int correctAnswerIndex = binding.spinnerCorrectAnswer.getSelectedItemPosition();
        Difficulty difficulty = Difficulty.valueOf(binding.spinnerDifficulty.getSelectedItem().toString());

        if (quesId == null) {
            // Add new question
            AddQuestionRequest request = new AddQuestionRequest(questionText, answerOptions, correctAnswerIndex, difficulty);
            viewModel.addQuestion(request, currentUserId);
        } else {
            // Update existing question
            UpdateQuestionRequest request = new UpdateQuestionRequest(quesId, questionText, answerOptions, correctAnswerIndex, difficulty);
            viewModel.updateQuestion(request, currentUserId);
        }
    }

    private void deleteQuestion() {
        if (quesId != null) {
            viewModel.deleteQuestion(quesId);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
