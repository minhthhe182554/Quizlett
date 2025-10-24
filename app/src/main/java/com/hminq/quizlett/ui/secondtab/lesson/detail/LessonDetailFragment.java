package com.hminq.quizlett.ui.secondtab.lesson.detail;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hminq.quizlett.R;
import com.hminq.quizlett.data.remote.model.LessonCategory;
import com.hminq.quizlett.data.remote.model.Question;
import com.hminq.quizlett.databinding.FragmentLessonDetailBinding;
import com.hminq.quizlett.ui.SharedViewModel;
import com.hminq.quizlett.ui.secondtab.lesson.adapter.QuestionDisplayAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LessonDetailFragment extends Fragment {

    private FragmentLessonDetailBinding binding;
    private LessonDetailViewModel viewModel;
    private SharedViewModel sharedViewModel;
    private NavController navController;
    private QuestionDisplayAdapter questionDisplayAdapter;

    private String lessonId = null;
    private String currentUserId;
    private ArrayList<String> selectedQuestionIds = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        navController = NavHostFragment.findNavController(this);
        viewModel = new ViewModelProvider(requireActivity()).get(LessonDetailViewModel.class);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        currentUserId = sharedViewModel.getCurrentUserUid();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLessonDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupSpinner();
        binding.spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String categoryStr = parent.getItemAtPosition(position).toString();
                LessonCategory category = LessonCategory.valueOf(categoryStr);
                viewModel.fetchQuestionsByCategory(category, currentUserId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        setupRecyclerView();
        setupClickListeners();

        if (getArguments() != null) {
            lessonId = getArguments().getString("lessonId");
        }

        if (lessonId != null) {

            binding.tvLessonDetailTitle.setText("Edit Lesson");
            viewModel.getLessonById(lessonId);
            binding.btnDeleteLesson.setVisibility(View.VISIBLE);

            binding.creationModeGroup.setVisibility(View.GONE);
            binding.tilQuestionCount.setVisibility(View.GONE);
            binding.btnSelectQuestions.setVisibility(View.VISIBLE);
            binding.btnSelectQuestions.setText("Manage Questions");
            binding.btnSelectQuestions.setEnabled(false);

        } else {
            binding.tvLessonDetailTitle.setText("Add Lesson");
            binding.btnDeleteLesson.setVisibility(View.GONE);
            binding.creationModeGroup.setVisibility(View.VISIBLE);
            setupCreationModeRadioListeners();
        }

        observeViewModel();
        setupFragmentResultListener();
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.lesson_category_array,
                R.layout.spiner_item
        );
        categoryAdapter.setDropDownViewResource(R.layout.spiner_item);
        binding.spinnerCategory.setAdapter(categoryAdapter);
    }

    private void setupRecyclerView() {
        questionDisplayAdapter = new QuestionDisplayAdapter();
        binding.rvQuestions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvQuestions.setAdapter(questionDisplayAdapter);
        binding.rvQuestions.setNestedScrollingEnabled(false);
    }

    private void setupCreationModeRadioListeners() {
        binding.rbAuto.setChecked(true);
        binding.tilQuestionCount.setVisibility(View.VISIBLE);
        binding.btnSelectQuestions.setVisibility(View.GONE);

        binding.rgCreationMode.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbAuto) {
                binding.tilQuestionCount.setVisibility(View.VISIBLE);
                binding.btnSelectQuestions.setVisibility(View.GONE);
                selectedQuestionIds.clear();
                viewModel.loadQuestionsByIds(new ArrayList<>());
            } else if (checkedId == R.id.rbManual) {
                binding.tilQuestionCount.setVisibility(View.GONE);
                binding.btnSelectQuestions.setVisibility(View.VISIBLE);
                binding.btnSelectQuestions.setText("Select Questions");
            }
        });
    }

    private void setupFragmentResultListener() {
        getParentFragmentManager().setFragmentResultListener("questionSelectionResult", getViewLifecycleOwner(), (requestKey, bundle) -> {
            ArrayList<String> resultIds = bundle.getStringArrayList("selectedQuestionIds");
            if (resultIds != null) {
                this.selectedQuestionIds = resultIds;
                viewModel.loadQuestionsByIds(selectedQuestionIds);
            }
        });
    }

    private void observeViewModel() {
        viewModel.getLesson().observe(getViewLifecycleOwner(), lesson -> {
            if (lesson != null && lessonId != null) {
                binding.etLessonTitle.setText(lesson.getTitle());
                int pos = Arrays.asList(getResources().getStringArray(R.array.lesson_category_array))
                        .indexOf(lesson.getCategory().name());
                if (pos != -1) binding.spinnerCategory.setSelection(pos);
            }
        });

        viewModel.getDisplayedQuestions().observe(getViewLifecycleOwner(), questions -> {
            boolean hasQuestions = questions != null && !questions.isEmpty();
            binding.tvQuestionsListTitle.setVisibility(hasQuestions ? View.VISIBLE : View.GONE);
            binding.rvQuestions.setVisibility(hasQuestions ? View.VISIBLE : View.GONE);
            questionDisplayAdapter.setQuestions(questions);

            if (questions != null) {
                selectedQuestionIds = questions.stream()
                        .map(Question::getQuesId)
                        .collect(Collectors.toCollection(ArrayList::new));
            }

            if (lessonId != null) {
                binding.btnSelectQuestions.setEnabled(true);
            }
        });

        viewModel.getActionSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                navController.popBackStack();
                viewModel.doneAction();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Log.e("LessonDetailFragment", "Error from ViewModel: " + msg);
                viewModel.doneError();
            }
        });
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> navController.popBackStack());

        binding.btnSelectQuestions.setOnClickListener(v -> {
            String categoryStr = binding.spinnerCategory.getSelectedItem().toString();
            LessonCategory category = LessonCategory.valueOf(categoryStr);
            Bundle bundle = new Bundle();
            bundle.putString("category", category.name());
            bundle.putStringArrayList("selectedQuestionIds", selectedQuestionIds);
            navController.navigate(R.id.action_lessonDetailFragment_to_questionSelectionFragment, bundle);
        });

        binding.btnSaveLesson.setOnClickListener(v -> saveLesson());
        binding.btnDeleteLesson.setOnClickListener(v -> {
            if (lessonId != null) viewModel.deleteLesson(lessonId);
        });
    }

    private void saveLesson() {
        binding.etLessonTitle.setError(null);
        binding.etQuestionCount.setError(null);

        String title = binding.etLessonTitle.getText().toString().trim();
        String categoryStr = binding.spinnerCategory.getSelectedItem().toString();
        LessonCategory category = LessonCategory.valueOf(categoryStr);

        if (title.isEmpty()) {
            binding.etLessonTitle.setError("Lesson title cannot be empty.");
            return;
        }

        if (lessonId == null) {
            if (binding.rbAuto.isChecked()) {
                String countStr = binding.etQuestionCount.getText().toString().trim();
                if (countStr.isEmpty()) {
                    binding.etQuestionCount.setError("Please enter number of questions.");
                    return;
                }
                try {
                    int questionCount = Integer.parseInt(countStr);
                    if (questionCount <= 0) {
                        binding.etQuestionCount.setError("Must be greater than 0.");
                        return;
                    }

                    List<Question> availableQuestions = viewModel.getQuestionsByCategory().getValue();
                    if (availableQuestions == null ||  availableQuestions.isEmpty()) {
                        binding.etQuestionCount.setError("No available questions in this category. Please choose another category.");
                        return;
                    }

                    if (questionCount > availableQuestions.size()) {
                        binding.etQuestionCount.setError("The number of questions (" + questionCount +
                                ") exceeds available questions (" + availableQuestions.size() + ").");
                        return;
                    }

                    viewModel.createAutoLesson(title, category, currentUserId, questionCount);
                } catch (NumberFormatException e) {
                    binding.etQuestionCount.setError("Invalid number.");
                }
            } else { 
                if (selectedQuestionIds.isEmpty()) {
                    binding.btnSelectQuestions.setError("Please select questions first.");
                    return;
                }
                viewModel.addLesson(title, category, currentUserId, selectedQuestionIds);
            }
        } else {
            viewModel.updateLesson(lessonId, title, category, selectedQuestionIds);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.clearDisplayedQuestions();
        binding = null;
    }
}
