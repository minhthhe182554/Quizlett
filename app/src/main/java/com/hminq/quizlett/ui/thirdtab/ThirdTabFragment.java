package com.hminq.quizlett.ui.thirdtab;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hminq.quizlett.R;
import com.hminq.quizlett.databinding.FragmentThirdTabBinding;

public class ThirdTabFragment extends Fragment {
    private FragmentThirdTabBinding binding;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    public ThirdTabFragment() {
    }

    public static ThirdTabFragment newInstance(String param1, String param2) {
        ThirdTabFragment fragment = new ThirdTabFragment();
        // TODO: Thêm logic Bundle nếu cần
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
        View view = binding.getRoot();

        binding.btnAddFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateFolderDialog();
            }
        });

        return view;
    }
    private void showCreateFolderDialog() {
        Context context = requireContext();

        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_create_folder, null);

        final EditText etFolderName = dialogView.findViewById(R.id.et_folder_name);
        final Button btnAddFolder = dialogView.findViewById(R.id.btnAddFolder);

        final AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .create();
        btnAddFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String folderName = etFolderName.getText().toString().trim();

                if (!folderName.isEmpty()) {
                    // TODO: THỰC HIỆN LOGIC LƯU FOLDER (ví dụ: vào ViewModel/Database)
                    Toast.makeText(context, "Đã tạo folder: " + folderName, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(context, "Tên thư mục không được để trống!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}