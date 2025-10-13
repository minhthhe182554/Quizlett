package com.hminq.quizlett.ui.secondtab.question.list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.hminq.quizlett.R;
import com.hminq.quizlett.data.remote.model.LessonCategory;
import com.hminq.quizlett.databinding.FragmentQuestionListBinding;
import com.hminq.quizlett.data.remote.model.Question;
import com.hminq.quizlett.ui.SharedViewModel;
import com.hminq.quizlett.ui.secondtab.question.adapter.QuestionAdapter;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class QuestionListFragment extends Fragment implements QuestionAdapter.OnItemClickListener {
    private static final String TAG = "FRAGMENT_QUESTION_LIST";
    private FragmentQuestionListBinding binding;
    private QuestionListViewModel viewModel;
    private SharedViewModel sharedViewModel;
    private QuestionAdapter adapter;
    private NavController navController;
    private String currentUserId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        navController = NavHostFragment.findNavController(this);
        viewModel = new ViewModelProvider(this).get(QuestionListViewModel.class);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        currentUserId = sharedViewModel.getCurrentUserUid();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentQuestionListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupClickListeners();
        setupFilterControls();
        observeViewModel();

        viewModel.loadQuestions(currentUserId);
    }

    private void setupRecyclerView() {
        adapter = new QuestionAdapter(new ArrayList<>(), this);
        binding.recyclerViewQuestions.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.btnAddQuestion.setOnClickListener(v -> {
            navController.navigate(R.id.action_questionListFragment_to_questionDetailFragment);
        });

        binding.btnBack.setOnClickListener(v -> {
            navController.navigate(R.id.action_questionListFragment_to_dashBoardFragment);
        });
    }

    private void setupFilterControls() {

        binding.searchViewQuestions.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                viewModel.setSearchQuery(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.setSearchQuery(newText);
                return true;
            }
        });

        List<String> categoryOptions = new ArrayList<>();
        categoryOptions.add("All Categories"); 

        for (LessonCategory c : LessonCategory.values()) {
            categoryOptions.add(c.name());
        }

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(), R.layout.spiner_item, categoryOptions);
        categoryAdapter.setDropDownViewResource(R.layout.spiner_item);
        binding.spinnerCategoryFilter.setAdapter(categoryAdapter);

        binding.spinnerCategoryFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    viewModel.setCategoryFilter(null);
                } else {
                    String selectedCategoryStr = parent.getItemAtPosition(position).toString();
                    try {
                        LessonCategory selectedCategory = LessonCategory.valueOf(selectedCategoryStr);
                        viewModel.setCategoryFilter(selectedCategory);
                    } catch (IllegalArgumentException e) {
                        viewModel.setCategoryFilter(null);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                viewModel.setCategoryFilter(null);
            }
        });
    }

    private void observeViewModel() {
        viewModel.getFilteredQuestions().observe(getViewLifecycleOwner(), questions -> {
            if (questions != null) {
                adapter.updateQuestions(questions);
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemClick(Question question) {
        Bundle bundle = new Bundle();
        bundle.putString("quesId", question.getQuesId()); // Reverted

        navController.navigate(R.id.action_questionListFragment_to_questionDetailFragment, bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
