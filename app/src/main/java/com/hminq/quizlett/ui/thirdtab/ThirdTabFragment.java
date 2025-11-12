package com.hminq.quizlett.ui.thirdtab;

import static com.google.firebase.appcheck.internal.util.Logger.TAG;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.hminq.quizlett.R;
import com.hminq.quizlett.data.remote.model.Folder;
import com.hminq.quizlett.databinding.DialogCreateFolderBinding;
import com.hminq.quizlett.databinding.FragmentThirdTabBinding;
import com.hminq.quizlett.databinding.ItemFolderBinding;
import com.hminq.quizlett.ui.thirdtab.folder.FolderViewModel;
import com.hminq.quizlett.ui.thirdtab.folder.FolderViewModel.CreationResult;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ThirdTabFragment extends Fragment {

    private static final String FRAGMENT_TAG = "ThirdTabFragment";

    private FragmentThirdTabBinding binding;
    private AlertDialog createFolderDialog;
    private DialogCreateFolderBinding dialogBinding;
    private FolderViewModel viewModel;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    public ThirdTabFragment() {
    }

    public static ThirdTabFragment newInstance(String param1, String param2) {
        ThirdTabFragment fragment = new ThirdTabFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
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


        viewModel = new ViewModelProvider(this).get(FolderViewModel.class);
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
//
//        if (folders.isEmpty()) {
//            // 💡 Logic hiển thị thông báo "Chưa có folder nào"
//            if (binding. != null) {
//                binding.emptyTextView.setVisibility(View.VISIBLE);
//            }
//            return;
//        } else {
//            if (binding.emptyTextView != null) {
//                binding.emptyTextView.setVisibility(View.GONE);
//            }
//        }

        for (Folder folder : folders) {
            ItemFolderBinding itemBinding = ItemFolderBinding.inflate(getLayoutInflater(), foldersContainer, false);

            itemBinding.folderName.setText(folder.getName());
            itemBinding.itemCount.setText(folder.getLessonCount() + " mục");
            itemBinding.userName.setText(folder.getUserName());

            itemBinding.getRoot().setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Mở folder: " + folder.getName(), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(requireContext(), "Lỗi hệ thống khi mở dialog.", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(requireContext(), "Tạo folder thành công!", Toast.LENGTH_SHORT).show();
                    break;
                case INVALID_INPUT:
                    Toast.makeText(requireContext(), "Tên folder không được để trống!", Toast.LENGTH_LONG).show();
                    break;
                case ERROR:
                    Toast.makeText(requireContext(), "Lỗi hệ thống khi tạo folder. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
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