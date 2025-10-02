package com.hminq.quizlett.ui.secondtab.question.list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.widget.SearchView; // Import SearchView

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.hminq.quizlett.R;
import com.hminq.quizlett.data.remote.model.Difficulty; // Import Difficulty
import com.hminq.quizlett.databinding.FragmentQuestionListBinding;
import com.hminq.quizlett.data.remote.model.Question;
import com.hminq.quizlett.ui.secondtab.question.adapter.QuestionAdapter;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class QuestionListFragment extends Fragment implements QuestionAdapter.OnItemClickListener {

    private FragmentQuestionListBinding binding;
    private QuestionListViewModel viewModel;
    private QuestionAdapter adapter;
    private NavController navController;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentQuestionListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        viewModel = new ViewModelProvider(this).get(QuestionListViewModel.class);

        setupRecyclerView();
        setupClickListeners();
        setupFilterControls();
        observeViewModel();

        viewModel.loadQuestions();
    }

    private void setupRecyclerView() {
        adapter = new QuestionAdapter(new ArrayList<>(), this);
        binding.recyclerViewQuestions.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.fabAddQuestion.setOnClickListener(v -> {
            navController.navigate(R.id.action_tab2_questionList_to_questionDetail);
        });

        binding.btnBack.setOnClickListener(v -> {
            navController.popBackStack();
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


        List<String> difficultyOptions = new ArrayList<>();
        difficultyOptions.add("All Difficulties");
        for (Difficulty d : Difficulty.values()) {
            difficultyOptions.add(d.name());
        }
        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(requireContext(), R.layout.spiner_item, difficultyOptions);
        difficultyAdapter.setDropDownViewResource(R.layout.spiner_item);
        binding.spinnerDifficultyFilter.setAdapter(difficultyAdapter);

        binding.spinnerDifficultyFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    viewModel.setDifficultyFilter(null);
                } else {
                    String selectedDifficultyStr = parent.getItemAtPosition(position).toString();
                    try {
                        Difficulty selectedDifficulty = Difficulty.valueOf(selectedDifficultyStr);
                        viewModel.setDifficultyFilter(selectedDifficulty);
                    } catch (IllegalArgumentException e) {
                        viewModel.setDifficultyFilter(null);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                viewModel.setDifficultyFilter(null);
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
        bundle.putString("quesId", question.getQuesId());
        // Navigate to QuestionDetailFragment for editing/viewing (using new action ID)
        navController.navigate(R.id.action_tab2_questionList_to_questionDetail, bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}