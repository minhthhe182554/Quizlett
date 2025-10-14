package com.hminq.quizlett.ui.secondtab.lesson.list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;


import com.hminq.quizlett.R;
import com.hminq.quizlett.data.remote.model.Lesson;
import com.hminq.quizlett.data.remote.model.LessonCategory;


import com.hminq.quizlett.databinding.FragmentLessonListBinding;
import com.hminq.quizlett.ui.SharedViewModel;
import com.hminq.quizlett.ui.secondtab.lesson.adapter.LessonAdapter;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LessonListFragment extends Fragment implements LessonAdapter.OnItemClickListener {

    private FragmentLessonListBinding binding;
    private LessonListViewModel viewModel;
    private SharedViewModel sharedViewModel;
    private LessonAdapter adapter;
    private NavController navController;
    private String currentUserId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        navController = NavHostFragment.findNavController(this);
        viewModel = new ViewModelProvider(this).get(LessonListViewModel.class);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        currentUserId = sharedViewModel.getCurrentUserUid();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLessonListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupFilterControls();
        setupClickListeners();
        observeViewModel();

        viewModel.loadLessons(currentUserId);
    }

    private void setupRecyclerView() {
        adapter = new LessonAdapter(new ArrayList<>(), this);
        binding.recyclerViewLessons.setAdapter(adapter);
    }

    private void setupFilterControls() {

        binding.searchViewLessons.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

        List<String> categories = new ArrayList<>();
        categories.add("All Categories");
        for (LessonCategory c : LessonCategory.values()) {
            categories.add(c.name());
        }

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(),
                R.layout.spiner_item, categories);
        categoryAdapter.setDropDownViewResource(R.layout.spiner_item);
        binding.spinnerCategoryFilter.setAdapter(categoryAdapter);

        binding.spinnerCategoryFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    viewModel.setCategoryFilter(null);
                } else {
                    try {
                        LessonCategory selected = LessonCategory.valueOf(categories.get(position));
                        viewModel.setCategoryFilter(selected);
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

    private void setupClickListeners() {
        binding.btnAddLesson.setOnClickListener(v ->
                navController.navigate(R.id.action_lessonListFragment_to_lessonDetailFragment));

        binding.btnBack.setOnClickListener(v ->
                navController.navigate(R.id.action_lessonListFragment_to_dashBoardFragment));
    }

    private void observeViewModel() {
        viewModel.getFilteredLessons().observe(getViewLifecycleOwner(), lessons -> {
            if (lessons != null) {
                adapter.updateLessons(lessons);
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemClick(Lesson lesson) {
        Bundle bundle = new Bundle();
        bundle.putString("lessonId", lesson.getLessonId());
        navController.navigate(R.id.action_lessonListFragment_to_lessonDetailFragment, bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
