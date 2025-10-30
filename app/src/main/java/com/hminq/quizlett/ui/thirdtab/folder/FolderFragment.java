package com.hminq.quizlett.ui.thirdtab.folder;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.hminq.quizlett.R; // Cần import R
import com.hminq.quizlett.data.remote.model.Folder;
import com.hminq.quizlett.databinding.DialogCreateFolderBinding;
import com.hminq.quizlett.databinding.FragmentThirdTabBinding; // Sử dụng FragmentThirdTabBinding
import com.hminq.quizlett.databinding.ItemFolderBinding; // Cần ItemFolderBinding (item_folder.xml)
import com.hminq.quizlett.ui.thirdtab.folder.FolderViewModel;
import com.hminq.quizlett.ui.thirdtab.folder.FolderViewModel.CreationResult;

import java.util.List;

public class FolderFragment extends Fragment {

    private FolderViewModel viewModel;
    private FragmentThirdTabBinding binding;
    private AlertDialog createFolderDialog;
    private LinearLayout foldersContainer; // Tham chiếu đến LinearLayout
    private static final String TAG = "FolderFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentThirdTabBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Tìm LinearLayout đã thêm ID trong XML
        foldersContainer = root.findViewById(R.id.folders_list_container);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(FolderViewModel.class);

        binding.btnAddFolder.setOnClickListener(v -> showCreateFolderDialog());

        observeCreationResult();
        observeFolderData(); // Bổ sung logic tải dữ liệu
    }

    /**
     * Quan sát danh sách folder từ ViewModel và hiển thị chúng bằng cách thêm View động.
     */
    private void observeFolderData() {
        viewModel.folders.observe(getViewLifecycleOwner(), folders -> {
            if (folders != null) {
                displayFoldersInLayout(folders);
            } else {
                Log.e(TAG, "Failed to load folders or list is null.");
                if (foldersContainer != null) {
                    foldersContainer.removeAllViews();
                }
            }
        });
    }

    /**
     * Xóa các View cũ và thêm các item folder mới (sử dụng ItemFolderBinding) vào LinearLayout.
     */
    private void displayFoldersInLayout(List<Folder> folders) {
        if (foldersContainer == null) {
            Log.e(TAG, "foldersContainer is null. Cannot display folders.");
            return;
        }

        foldersContainer.removeAllViews();

        for (Folder folder : folders) {
            ItemFolderBinding itemBinding = ItemFolderBinding.inflate(getLayoutInflater(), foldersContainer, false);

            itemBinding.folderName.setText(folder.getName());

            int itemCount = folder.getLessons() != null ? folder.getLessons().size() : 0;
            itemBinding.itemCount.setText(itemCount + " mục");
            itemBinding.userName.setText("");

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            if (foldersContainer.getChildCount() > 0) {
                int marginDp = 16;
                float density = getResources().getDisplayMetrics().density;
                int marginPx = (int) (marginDp * density + 0.5f);
                layoutParams.topMargin = marginPx;
            }
            itemBinding.getRoot().setLayoutParams(layoutParams);

            itemBinding.getRoot().setOnClickListener(v -> onFolderClick(folder));

            foldersContainer.addView(itemBinding.getRoot());
        }
    }

    private void onFolderClick(Folder folder) {
        Toast.makeText(requireContext(), "Mở folder: " + folder.getName(), Toast.LENGTH_SHORT).show();
    }

    private void showCreateFolderDialog() {
        DialogCreateFolderBinding dialogBinding = DialogCreateFolderBinding.inflate(getLayoutInflater());

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogBinding.getRoot());

        createFolderDialog = builder.create();

        dialogBinding.btnAddFolder.setOnClickListener(v -> {
            String folderName = dialogBinding.etFolderName.getText().toString().trim();
            viewModel.createFolder(folderName);
        });

        createFolderDialog.show();
    }

    private void observeCreationResult() {
        viewModel.creationResult.observe(getViewLifecycleOwner(), result -> {
            if (createFolderDialog != null && createFolderDialog.isShowing()) {
                if (result != CreationResult.LOADING) {
                    createFolderDialog.dismiss();
                }
            }

            switch (result) {
                case LOADING:
                    break;
                case SUCCESS:
                    Toast.makeText(requireContext(), "Tạo folder thành công!", Toast.LENGTH_SHORT).show();
                    break;
                case INVALID_INPUT:
                    Toast.makeText(requireContext(), "Tên folder không được để trống!", Toast.LENGTH_LONG).show();
                    break;
                case ERROR:
                    Toast.makeText(requireContext(), "Lỗi hệ thống khi tạo folder. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (createFolderDialog != null) {
            createFolderDialog.dismiss();
        }
    }
}