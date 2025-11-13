package com.hminq.quizlett.ui.thirdtab.detail;

import android.app.AlertDialog;
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
import com.hminq.quizlett.ui.MainTabViewModel;
import com.hminq.quizlett.ui.secondtab.lesson.adapter.LessonAdapter;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FolderDetailFragment extends Fragment implements LessonAdapter.OnItemClickListener {

    private static final String TAG = "FolderDetailFragment";
    public static final String ARG_FOLDER_ID = "folder_id";

    private FragmentFolderDetailBinding binding;
    private FolderDetailViewModel viewModel;
    private MainTabViewModel mainTabViewModel;

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

        Log.d("DEBUG_NAV", "Đã nhận được ID: " + folderId);

        if (getArguments() != null) {
            folderId = getArguments().getString(ARG_FOLDER_ID);
        }

        if (folderId == null) {
            Log.e(TAG, "Error: Folder ID not found.");
            folderId = "mock_folder_id";
        }

        viewModel = new ViewModelProvider(this).get(FolderDetailViewModel.class);
        mainTabViewModel = new ViewModelProvider(requireActivity()).get(MainTabViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
        
        binding.btnDeleteFolder.setOnClickListener(v -> showDeleteConfirmationDialog());
    }
    
    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_folder_title)
            .setMessage(R.string.delete_folder_message)
            .setPositiveButton(R.string.delete_button, (dialog, which) -> {
                if (folderId != null) {
                    viewModel.deleteFolder(folderId);
                }
            })
            .setNegativeButton(R.string.cancel_button, null)
            .show();
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
            if (binding.btnDeleteFolder != null) {
                binding.btnDeleteFolder.setEnabled(!isLoading);
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });
        
        viewModel.getDeleteSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(getContext(), R.string.folder_deleted_success, Toast.LENGTH_SHORT).show();
                if (navController != null) {
                    navController.popBackStack();
                }
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
        Log.d(TAG, "Requesting navigation to Tab 1 to view lesson detail");
        
        // Use MainTabViewModel to coordinate between tabs
        mainTabViewModel.requestViewLessonDetail(lesson);
    }
}