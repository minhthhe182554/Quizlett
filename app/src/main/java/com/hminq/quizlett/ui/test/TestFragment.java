package com.hminq.quizlett.ui.test;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.hminq.quizlett.R;
import com.hminq.quizlett.data.remote.model.Lesson;
import com.hminq.quizlett.databinding.FlagmentTestBinding;

public class TestFragment extends Fragment {

    private FlagmentTestBinding binding;
    private Lesson lesson;
    private NavController navController;

    public TestFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            lesson = (Lesson) getArguments().getSerializable("lesson");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FlagmentTestBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = NavHostFragment.findNavController(this);

        if (lesson == null) {
            Toast.makeText(getContext(), "Error: Lesson not found", Toast.LENGTH_SHORT).show();
            navController.popBackStack();
            return;
        }

        setupViews();
        setupListeners();
    }

    private void setupViews() {
        binding.lessonTitle.setText(lesson.getTitle());

        // Setup question count picker
        binding.questionCountText.setText("1");

        // Setup test method dropdown
        String[] testMethods = new String[]{"Multiple Choice", "T/F"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, testMethods);
        ((AutoCompleteTextView) binding.testMethodDropdown.getEditText()).setAdapter(adapter);
        ((AutoCompleteTextView) binding.testMethodDropdown.getEditText()).setText(testMethods[0], false);
    }

    private void setupListeners() {
        binding.closeButton.setOnClickListener(v -> navController.popBackStack());

        binding.upArrow.setOnClickListener(v -> {
            int maxQuestions = lesson.getQuestions() != null ? lesson.getQuestions().size() : 1;
            int currentCount = Integer.parseInt(binding.questionCountText.getText().toString());
            if (currentCount < maxQuestions) {
                binding.questionCountText.setText(String.valueOf(currentCount + 1));
            }
        });

        binding.downArrow.setOnClickListener(v -> {
            int currentCount = Integer.parseInt(binding.questionCountText.getText().toString());
            if (currentCount > 1) {
                binding.questionCountText.setText(String.valueOf(currentCount - 1));
            }
        });

        binding.startTestButton.setOnClickListener(v -> {
            String testMethod = ((AutoCompleteTextView) binding.testMethodDropdown.getEditText()).getText().toString();
            int questionCount = Integer.parseInt(binding.questionCountText.getText().toString());

            Bundle bundle = new Bundle();
            bundle.putSerializable("lesson", lesson);
            bundle.putInt("questionCount", questionCount);

            if (testMethod.equals("Multiple Choice")) {
                navController.navigate(R.id.action_testFragment_to_multipleChoiceTestFragment, bundle);
            } else {
                navController.navigate(R.id.action_testFragment_to_tfTestFragment, bundle);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
