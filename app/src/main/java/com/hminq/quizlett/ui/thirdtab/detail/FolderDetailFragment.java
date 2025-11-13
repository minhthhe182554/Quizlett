package com.hminq.quizlett.ui.thirdtab.detail;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.hminq.quizlett.R;
import com.hminq.quizlett.databinding.FragmentFolderDetailBinding;
import com.hminq.quizlett.data.remote.model.Lesson;
import com.hminq.quizlett.ui.secondtab.lesson.adapter.LessonAdapter;

import java.util.ArrayList;

public class FolderDetailFragment extends Fragment implements LessonAdapter.OnItemClickListener {

    private static final String TAG = "FolderDetailFragment";
    public static final String ARG_FOLDER_ID = "folder_id";

    private FragmentFolderDetailBinding binding;
    private FolderDetailViewModel viewModel;

    private String folderId;
    private LessonAdapter lessonAdapter;
    private NavController navController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFolderDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        folderId = FolderDetailFragmentArgs.fromBundle(getArguments()).getFolderId();

        Log.d("DEBUG_NAV", "Đã nhận được ID thật: " + folderId);

        viewModel = new ViewModelProvider(this).get(FolderDetailViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(FolderDetailViewModel.class);

        try {
            navController = NavHostFragment.findNavController(this);
        } catch (IllegalStateException e) {
            Log.e(TAG, "NavController not found. Navigation Component error occurred.", e);
        }

        lessonAdapter = new LessonAdapter(new ArrayList<>(), this);
        binding.rvLessonsList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvLessonsList.setAdapter(lessonAdapter);

        observeViewModel();

        viewModel.loadFolderAndLessons(folderId);


        binding.btnBack.setOnClickListener(v -> {
            if (navController != null) {
                navController.popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });
    }

    private void observeViewModel() {
        viewModel.getFolder().observe(getViewLifecycleOwner(), folder -> {
            if (folder != null) {
                binding.tvFolderDetailTitle.setText(folder.getName());
            }
        });

        viewModel.getLessons().observe(getViewLifecycleOwner(), lessons -> {
            if (lessons != null) {
                lessonAdapter.updateLessons(lessons);
            }
        });

        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onItemClick(Lesson lesson) {
        Log.d(TAG, "Lesson clicked: " + lesson.getTitle() + " (ID: " + lesson.getLessonId() + ")");

        if (lesson.getLessonId() == null || lesson.getLessonId().isEmpty()) {
            Log.e(TAG, "LỖI: Lesson ID bị null, không thể điều hướng.");
            Toast.makeText(getContext(), "Lỗi: Dữ liệu bài học không hợp lệ.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (navController != null) {
            FolderDetailFragmentDirections.ActionFolderDetailFragmentToLessonDetailFragment action =
                    FolderDetailFragmentDirections.actionFolderDetailFragmentToLessonDetailFragment(lesson);
            try {
                navController.navigate(action);
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi điều hướng đến LessonDetailFragment", e);
            }

        } else {
            Toast.makeText(getContext(), "Lỗi: Hệ thống điều hướng chưa sẵn sàng.", Toast.LENGTH_SHORT).show();
        }
    }
}