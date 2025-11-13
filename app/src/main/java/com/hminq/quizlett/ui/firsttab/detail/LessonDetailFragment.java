package com.hminq.quizlett.ui.firsttab.detail;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.hminq.quizlett.R;
import com.hminq.quizlett.data.remote.model.Lesson;
import com.hminq.quizlett.databinding.FragmentLessonDetail2Binding;
import com.hminq.quizlett.ui.firsttab.detail.adapter.QuestionDetailAdapter;
import com.hminq.quizlett.utils.ImageLoader;
import com.hminq.quizlett.utils.Message;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LessonDetailFragment extends Fragment {
    private static final String TAG = "FRAGMENT_LESSON_DETAIL";
    private FragmentLessonDetail2Binding binding;
    private Lesson clickedLesson;
    private NavController navController;
    private QuestionDetailAdapter questionAdapter;
    private DetailViewModel detailViewModel;

    public LessonDetailFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        navController = NavHostFragment.findNavController(this);

        if (getArguments() != null) {
            clickedLesson = getArguments().getSerializable("lesson", Lesson.class);
        }

        if (clickedLesson != null) {
            Log.d(TAG, "Clicked lesson id: " + clickedLesson.getLessonId());
        }
        else {
            navController.popBackStack(); //navigate back to Home
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLessonDetail2Binding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = NavHostFragment.findNavController(this);
        detailViewModel = new ViewModelProvider(this).get(DetailViewModel.class);

        if (clickedLesson == null) {
            Toast.makeText(getContext(), "Error: Lesson not found", Toast.LENGTH_SHORT).show();
            navController.popBackStack();
            return;
        }

        setupViews();
        setupRecyclerView();
        setupButtons();
        observeViewModel();
        loadCreatorInfo();
    }

    private void setupViews() {
        // Set lesson title
        binding.tvLessonTitle.setText(clickedLesson.getTitle());

        // Set category
        if (clickedLesson.getCategory() != null) {
            binding.tvCategory.setText(clickedLesson.getCategory().name());
        } else {
            binding.tvCategory.setText("Others");
        }
// Set question count
        int questionCount = clickedLesson.getQuestions() != null
                ? clickedLesson.getQuestions().size()
                : 0;
        binding.tvQuestionCount.setText(questionCount + " Questions");

        // Load creator image (already have URL)
        if (clickedLesson.getCreatorImage() != null) {
            ImageLoader.loadImage(binding.ivCreatorImage, clickedLesson.getCreatorImage());
        }
    }

    private void setupRecyclerView() {
        questionAdapter = new QuestionDetailAdapter();
        binding.rvQuestionList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvQuestionList.setAdapter(questionAdapter);

        if (clickedLesson.getQuestions() != null) {
            questionAdapter.setQuestions(clickedLesson.getQuestions());
        }
    }

    private void setupButtons() {
        // Back button
        binding.btnBack.setOnClickListener(v -> {
            navController.popBackStack();
        });

        // TODO: Add button
        binding.btnAdd.setOnClickListener(v -> {
            // TODO: Implement add to collection functionality
            Toast.makeText(getContext(), "TODO: Add to collection", Toast.LENGTH_SHORT).show();
        });

        // TODO: Test button
        binding.btnTest.setOnClickListener(v -> {
            Toast.makeText(getContext(), "TODO: Start test", Toast.LENGTH_SHORT).show();
            // navigation to lesson detail
            Bundle createTestLesson = new Bundle();

            createTestLesson.putSerializable("create_lesson", clickedLesson);
            navController.navigate(R.id.action_lessonDetailFragment3_to_createTestFragment, createTestLesson);
        });
    }

    private void observeViewModel() {
        // Observe creator info
        detailViewModel.getCreator().observe(getViewLifecycleOwner(), user -> {
            if (user != null && user.getFullname() != null) {
                binding.tvCreatorName.setText(user.getFullname());
            } else {
                binding.tvCreatorName.setText("Unknown");
            }
        });

        // Observe errors
        detailViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Log.e(TAG, "Error: " + error);
                binding.tvCreatorName.setText("Unknown");
            }
        });
    }

    private void loadCreatorInfo() {
        String userId = clickedLesson.getUserId();
        if (userId == null) {
            binding.tvCreatorName.setText("Unknown");
            return;
        }

        // Load creator info via ViewModel
        detailViewModel.loadCreatorInfo(userId);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}