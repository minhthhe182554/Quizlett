package com.hminq.quizlett.ui.firsttab.test;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.hminq.quizlett.R;
import com.hminq.quizlett.data.remote.model.Lesson;
import com.hminq.quizlett.databinding.FragmentCreateTestBinding;
import com.hminq.quizlett.utils.ImageLoader;

public class CreateTestFragment extends Fragment {
    private static final String TAG = "FRAGMENT_CREATE_TEST";
    private FragmentCreateTestBinding binding;
    private NavController navController;
    private Lesson createTestLesson;
    private int selectedQuestionCount = 1;
    private String selectedTestMethod = "T/F"; // Default

    public CreateTestFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            createTestLesson = getArguments().getSerializable("create_lesson", Lesson.class);
        }

        if (createTestLesson != null) {
            Log.d(TAG, "Create test for lesson: " + createTestLesson.getLessonId());
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCreateTestBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        navController = NavHostFragment.findNavController(this);
        
        if (createTestLesson == null) {
            Toast.makeText(getContext(), "Error: Lesson not found", Toast.LENGTH_SHORT).show();
            navController.popBackStack();
            return;
        }
        
        setupViews();
        setupNumberPicker();
        setupTestMethodSpinner();
        setupButtons();
    }
    
    private void setupViews() {
        // Set lesson title
        binding.tvLessonTitle.setText(createTestLesson.getTitle());
        
        // Load creator image
        if (createTestLesson.getCreatorImage() != null) {
            ImageLoader.loadImage(binding.ivCreatorImage, createTestLesson.getCreatorImage());
        }
    }
    
    private void setupNumberPicker() {
        int maxQuestions = (createTestLesson.getQuestions() != null) 
                ? createTestLesson.getQuestions().size() 
                : 1;
        
        // Configure NumberPicker
        binding.countPicker.setMinValue(1);
        binding.countPicker.setMaxValue(maxQuestions);
        binding.countPicker.setValue(1); // Default value
        binding.countPicker.setWrapSelectorWheel(false);
        
        // Style NumberPicker - apply theme colors
        styleNumberPicker();
        
        // Listen to value changes
        binding.countPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            selectedQuestionCount = newVal;
            Log.d(TAG, "Selected question count: " + selectedQuestionCount);
        });
        
        selectedQuestionCount = 1;
    }
    
    private void styleNumberPicker() {
        // Apply white text color and larger size to NumberPicker
        binding.countPicker.post(() -> {
            for (int i = 0; i < binding.countPicker.getChildCount(); i++) {
                View child = binding.countPicker.getChildAt(i);
                if (child instanceof android.widget.EditText) {
                    android.widget.EditText editText = (android.widget.EditText) child;
                    editText.setTextColor(Color.WHITE);
                    editText.setTextSize(24);
                }
            }
        });
    }
    
    private void setupTestMethodSpinner() {
        // Default hint for T/F (first item)
        binding.hint.setText(R.string.tf_hint);
        selectedTestMethod = "T/F";
        
        binding.testMethodPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String method = parent.getItemAtPosition(position).toString();
                selectedTestMethod = method;
                
                // Update hint based on selected method
                if (method.equals("T/F")) {
                    binding.hint.setText(R.string.tf_hint);
                } else if (method.equals("Multiple choice")) {
                    binding.hint.setText(R.string.multiple_choice_hint);
                }
                
                Log.d(TAG, "Selected test method: " + selectedTestMethod);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Default to T/F
                binding.hint.setText(R.string.tf_hint);
            }
        });
    }
    
    private void setupButtons() {
        // Back button
        binding.btnBack.setOnClickListener(v -> {
            navController.popBackStack();
        });
        
        // Start test button
        binding.btnStart.setOnClickListener(v -> {
            // TODO: Navigate to test screen with selected options
            String message = "Starting test:\n" +
                    "Questions: " + selectedQuestionCount + "\n" +
                    "Method: " + selectedTestMethod;
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            Log.d(TAG, message);
        });
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
