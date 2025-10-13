package com.hminq.quizlett.ui.secondtab.lesson.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hminq.quizlett.data.remote.model.LessonCategory;
import com.hminq.quizlett.data.remote.model.Question;
import com.hminq.quizlett.databinding.FragmentQuestionSelectionBinding;
import com.hminq.quizlett.ui.SharedViewModel;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class QuestionSelectionFragment extends Fragment {

    private FragmentQuestionSelectionBinding binding;
    private LessonDetailViewModel viewModel;
    private SharedViewModel sharedViewModel;
    private NavController navController;
    private QuestionSelectionAdapter adapter;
    private LessonCategory category;
    private String currentUserId;
    private ArrayList<String> selectedQuestionIds = new ArrayList<>();
    private ArrayList<String> initialSelectedIds = new ArrayList<>(); // To hold passed-in selections

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        navController = NavHostFragment.findNavController(this);
        viewModel = new ViewModelProvider(requireActivity()).get(LessonDetailViewModel.class);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        currentUserId = sharedViewModel.getCurrentUserUid();

        if (getArguments() != null) {
            String categoryStr = getArguments().getString("category");
            if (categoryStr != null) {
                category = LessonCategory.valueOf(categoryStr);
            }
            // UPDATED: Get the list of already selected question IDs
            initialSelectedIds = getArguments().getStringArrayList("selectedQuestionIds");
            if (initialSelectedIds != null) {
                selectedQuestionIds.addAll(initialSelectedIds);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentQuestionSelectionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupListeners();
        observeViewModel();

        if (category != null) {
            viewModel.fetchQuestionsByCategory(category, currentUserId);
        } else {
            Toast.makeText(requireContext(), "Category not found.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerView() {
        // UPDATED: Pass the initial selected IDs to the adapter
        adapter = new QuestionSelectionAdapter(this::onQuestionSelected, initialSelectedIds);
        binding.recyclerViewQuestions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewQuestions.setAdapter(adapter);
    }

    private void onQuestionSelected(Question question, boolean isChecked) {
        String questionId = question.getQuesId();
        if (isChecked) {
            if (!selectedQuestionIds.contains(questionId)) {
                selectedQuestionIds.add(questionId);
            }
        } else {
            selectedQuestionIds.remove(questionId);
        }
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> navController.popBackStack());

        binding.btnDone.setOnClickListener(v -> {
            Bundle result = new Bundle();
            result.putStringArrayList("selectedQuestionIds", selectedQuestionIds);
            getParentFragmentManager().setFragmentResult("questionSelectionResult", result);
            navController.popBackStack();
        });
    }

    private void observeViewModel() {
        viewModel.getQuestionsByCategory().observe(getViewLifecycleOwner(), questions -> {
            if (questions != null) {
                adapter.submitList(questions);
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        viewModel.clearQuestionsByCategory();
    }
}
