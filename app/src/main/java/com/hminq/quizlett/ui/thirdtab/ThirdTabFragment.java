package com.hminq.quizlett.ui.thirdtab;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.hminq.quizlett.R;
import com.hminq.quizlett.data.remote.model.Folder;
import com.hminq.quizlett.data.remote.model.Lesson;
import com.hminq.quizlett.databinding.DialogCreateFolderBinding;
import com.hminq.quizlett.databinding.FragmentThirdTabBinding;
import com.hminq.quizlett.databinding.ItemFolderBinding;
import com.hminq.quizlett.ui.MainTabViewModel;
import com.hminq.quizlett.ui.thirdtab.folder.FolderViewModel;
import com.hminq.quizlett.ui.thirdtab.folder.FolderViewModel.CreationResult;
import androidx.navigation.Navigation;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ThirdTabFragment extends Fragment {

    private static final String FRAGMENT_TAG = "ThirdTabFragment";

    private static final String ARG_LESSON_TO_SAVE = "lessonToSave";

    private FragmentThirdTabBinding binding;
    private AlertDialog createFolderDialog;
    private DialogCreateFolderBinding dialogBinding;
    private FolderViewModel viewModel;
    private NavController navController;
    private MainTabViewModel mainTabViewModel;

    private Lesson lessonToSave = null;
    private boolean isSavingMode = false;

    public ThirdTabFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainTabViewModel = new ViewModelProvider(requireActivity()).get(MainTabViewModel.class);

        if (getArguments() != null) {
            Lesson lessonFromArgs = (Lesson) getArguments().getSerializable(ARG_LESSON_TO_SAVE);
            if (lessonFromArgs != null) {
                lessonToSave = lessonFromArgs;
                isSavingMode = true;
                Log.d(FRAGMENT_TAG, "Nhận Lesson từ arguments: " + lessonToSave.getTitle());
            }
        }

        Lesson pendingLesson = mainTabViewModel.getLessonToSave().getValue();
        if (pendingLesson != null) {
            lessonToSave = pendingLesson;
            isSavingMode = true;
            Log.d(FRAGMENT_TAG, "Khởi tạo ở chế độ lưu với Lesson từ ViewModel: " + lessonToSave.getTitle());
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentThirdTabBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            navController = Navigation.findNavController(view);
            Log.d(FRAGMENT_TAG, "NavController được tìm thấy bằng Navigation.findNavController(view)");
        } catch (IllegalStateException e) {
            Log.e(FRAGMENT_TAG, "Lỗi khi tìm NavController bằng view, thử dùng NavHostFragment.findNavController(this).", e);
            try {
                navController = NavHostFragment.findNavController(this);
                Log.d(FRAGMENT_TAG, "NavController được tìm thấy bằng NavHostFragment.findNavController(this)");
            } catch (Exception ex) {
                Log.e(FRAGMENT_TAG, "Không thể tìm thấy NavController nào.", ex);
            }
        }

        viewModel = new ViewModelProvider(this).get(FolderViewModel.class);
        applySavingModeUi();
        mainTabViewModel.getLessonToSave().observe(getViewLifecycleOwner(), this::handleLessonToSave);

        if (binding.progressBar != null) {
            binding.progressBar.setVisibility(View.GONE);
        }

        if (binding.btnAddFolder != null) {
            binding.btnAddFolder.setOnClickListener(v -> {
                Log.d(FRAGMENT_TAG, "Nút Thêm Folder đã được nhấn. Mở dialog!");
                showCreateFolderDialog();
            });
        }

        observeCreationResult();
        observeFoldersList();
        observeLoadingState();
        observeSaveLessonResult();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload folders when returning from FolderDetailFragment (e.g. after deletion)
        Log.d(FRAGMENT_TAG, "onResume: Reloading folders list");
        if (viewModel != null) {
            viewModel.loadFolders();
        }
    }

    private void observeSaveLessonResult() {
        viewModel.getSaveLessonResult().observe(getViewLifecycleOwner(), isSuccess -> {
            if (isSuccess != null) {
                if (isSuccess) {
                    Toast.makeText(requireContext(), R.string.save_lesson_success, Toast.LENGTH_SHORT).show();

                    mainTabViewModel.clearLessonToSave();
                    mainTabViewModel.requestOpenTab(MainTabViewModel.TAB_INDEX_FIRST);
                } else {
                    Toast.makeText(requireContext(), R.string.save_lesson_error, Toast.LENGTH_LONG).show();
                }
                viewModel.resetSaveLessonResult();
            }
        });
    }

    private void handleLessonToSave(@Nullable Lesson lesson) {
        if (lesson != null) {
            lessonToSave = lesson;
            isSavingMode = true;
            Log.d(FRAGMENT_TAG, "Chuyển sang chế độ lưu với lesson: " + lesson.getTitle());
        } else {
            lessonToSave = null;
            isSavingMode = false;
            Log.d(FRAGMENT_TAG, "Thoát chế độ lưu thư mục.");
        }
        applySavingModeUi();
    }

    private void applySavingModeUi() {
        if (binding == null) {
            return;
        }

        // Always show back button
        binding.btnBack.setVisibility(View.VISIBLE);

        if (isSavingMode) {
            binding.tvHeader.setText(R.string.select_folder);
            binding.btnBack.setOnClickListener(v -> {
                mainTabViewModel.clearLessonToSave();
                mainTabViewModel.requestOpenTab(MainTabViewModel.TAB_INDEX_FIRST);
            });
        } else {
            binding.tvHeader.setText(R.string.my_library);
            binding.btnBack.setOnClickListener(v -> {
                // Navigate back or go to home tab
                mainTabViewModel.requestOpenTab(MainTabViewModel.TAB_INDEX_FIRST);
            });
        }
    }


    private void observeLoadingState() {
        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {

            if (binding.progressBar != null) {
                binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }

            if (binding.btnAddFolder != null) {
                binding.btnAddFolder.setEnabled(!isLoading);
            }
        });
    }

    private void observeFoldersList() {
        viewModel.folders.observe(getViewLifecycleOwner(), folders -> {

            if (folders != null) {
                displayFoldersInLayout(folders);
            }
        });
    }

    private void displayFoldersInLayout(List<Folder> folders) {
        LinearLayout foldersContainer = binding.foldersListContainer;
        if (foldersContainer == null) return;
        foldersContainer.removeAllViews();

        for (Folder folder : folders) {
            ItemFolderBinding itemBinding = ItemFolderBinding.inflate(getLayoutInflater(), foldersContainer, false);

            itemBinding.folderName.setText(folder.getName());
            itemBinding.itemCount.setText(folder.getLessonCount() + " " + getString(R.string.items_suffix));

            itemBinding.getRoot().setOnClickListener(v -> {
                if (isSavingMode) {
                    Log.d(FRAGMENT_TAG, "Đang lưu Lesson '" + lessonToSave.getTitle() + "' vào Folder: " + folder.getName());
                    if (lessonToSave != null) {
                        viewModel.saveLessonToFolder(lessonToSave, folder.getFolderId());
                    }
                } else {
                    Log.d(FRAGMENT_TAG, "Chuyển sang trang Folder Detail cho folder ID: " + folder.getFolderId());

                    ThirdTabFragmentDirections.ActionThirdTabFragmentToFolderDetailFragment action =
                            ThirdTabFragmentDirections.actionThirdTabFragmentToFolderDetailFragment(
                                    folder.getFolderId()
                            );

                    if (navController != null) {
                        navController.navigate(action);
                    } else {
                        Log.e(FRAGMENT_TAG, "NavController is null, cannot navigate!");
                    }
                }

            });

            foldersContainer.addView(itemBinding.getRoot());
        }
    }

    private void showCreateFolderDialog() {
        try {
            Context context = requireContext();

            dialogBinding = DialogCreateFolderBinding.inflate(getLayoutInflater());

            final AlertDialog dialog = new AlertDialog.Builder(context)
                    .setView(dialogBinding.getRoot())
                    .create();

            createFolderDialog = dialog;

            dialogBinding.btnAddFolder.setOnClickListener(v -> {
                String folderName = dialogBinding.etFolderName.getText().toString().trim();
                viewModel.createFolder(folderName);
            });

            dialog.show();
        } catch (Exception e) {
            Log.e(FRAGMENT_TAG, "LỖI KHI MỞ DIALOG:", e);
//            Toast.makeText(requireContext(), R.string.open_dialog_error, Toast.LENGTH_LONG).show();
        }
    }

    private void observeCreationResult() {
        viewModel.creationResult.observe(getViewLifecycleOwner(), result -> {

            handleDialogLoadingState(result == CreationResult.LOADING);

            if (result == CreationResult.LOADING) {
                return;
            }

            if (createFolderDialog != null && createFolderDialog.isShowing()) {
                createFolderDialog.dismiss();
            }

            switch (result) {
                case SUCCESS:
                    Toast.makeText(requireContext(), R.string.create_folder_success, Toast.LENGTH_SHORT).show();
                    break;
                case INVALID_INPUT:
                    Toast.makeText(requireContext(), R.string.folder_name_cannot_be_empty, Toast.LENGTH_LONG).show();
                    break;
                case ERROR:
                    Toast.makeText(requireContext(), R.string.create_folder_error, Toast.LENGTH_LONG).show();
                    break;
                default:
            }
        });
    }

    private void handleDialogLoadingState(boolean isLoading) {
        if (dialogBinding != null) {
            // Vô hiệu hóa nút tạo
            dialogBinding.btnAddFolder.setEnabled(!isLoading);

        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (createFolderDialog != null && createFolderDialog.isShowing()) {
            createFolderDialog.dismiss();
        }
        binding = null;
        dialogBinding = null;
    }
}