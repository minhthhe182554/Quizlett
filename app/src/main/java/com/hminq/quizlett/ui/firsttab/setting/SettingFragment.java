package com.hminq.quizlett.ui.firsttab.setting;

import static com.hminq.quizlett.constants.AppMessage.DELETE_ACCOUNT_ERROR;
import static com.hminq.quizlett.constants.AppMessage.SIGNOUT_ERROR;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hminq.quizlett.R;
import com.hminq.quizlett.data.remote.model.Language;
import com.hminq.quizlett.databinding.FragmentSettingBinding;
import com.hminq.quizlett.ui.SharedViewModel;
import com.hminq.quizlett.utils.Message;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingFragment extends Fragment {
    private FragmentSettingBinding binding;
    private SharedViewModel sharedViewModel;
    private SettingViewModel settingViewModel;
    private NavController navController;
    private ActivityResultLauncher<String> imagePickerLauncher;

    public SettingFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        settingViewModel = new ViewModelProvider(this).get(SettingViewModel.class);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        settingViewModel.updateProfileImage(uri);
                    }
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentSettingBinding.inflate(inflater, container, false);
        // Đã XÓA: bindViews();

        navController = NavHostFragment.findNavController(this);

        Context context = getContext();
        if (context != null) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                    context,
                    R.array.language_options_array,
                    android.R.layout.simple_spinner_item
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerLanguage.setAdapter(adapter);
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnBack.setOnClickListener(l -> navController.popBackStack());
        binding.btnSignOut.setOnClickListener(l -> sharedViewModel.signOut());
        binding.btnDelete.setOnClickListener(l -> {
            Message.showShort(view, "Đừng bấm vào chưa làm xong");
        });

        sharedViewModel.getSignOutErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            Message.showShort(view, getString(SIGNOUT_ERROR));
        });

        sharedViewModel.getDeleteErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            Message.showShort(view, getString(DELETE_ACCOUNT_ERROR));
        });

        settingViewModel.getProfileImageUrl().observe(getViewLifecycleOwner(), imageUrl -> {
            if (imageUrl != null && binding.userAvatar != null) {
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.welcome_img)
                        .error(R.drawable.welcome_img)
                        .into(binding.userAvatar);
            }
        });

        binding.userAvatar.setOnClickListener(l -> {
            imagePickerLauncher.launch("image/*");
        });

        binding.profileFullName.setOnClickListener(l -> {
            String currentValue = binding.profileFullName.getText().toString();
            showUpdateDialog("Full name", "fullname", currentValue, false);
        });

        binding.profilePassword.setOnClickListener(l -> {
            showUpdateDialog("New Password", "password", "", true);
        });

        settingViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                binding.profileDisplayName.setText(user.getFullname());
                binding.profileFullName.setText(user.getFullname());
                binding.profileEmail.setText(user.getEmail());

                binding.profilePassword.setText("********");

                String currentLanguageCode = user.getUserSetting() != null && user.getUserSetting().getLanguage() != null ?
                        user.getUserSetting().getLanguage().getCode() : "en";

                int selectionIndex = Language.getArrayIndex(currentLanguageCode);

                binding.spinnerLanguage.setOnItemSelectedListener(null);
                binding.spinnerLanguage.setSelection(selectionIndex);
                binding.spinnerLanguage.post(this::setupLanguageSpinnerListener);
            }
        });

        settingViewModel.getUpdateLanguageResult().observe(getViewLifecycleOwner(), success -> {
            if (success != null) {
                if (success) {
                    Message.showShort(view, "Cài đặt ngôn ngữ được lưu thành công! Đang khởi động lại...");

                    requireActivity().recreate();

                } else {
                    Message.showShort(view, "Lỗi khi lưu cài đặt ngôn ngữ.");
                }
            }
        });

        settingViewModel.getUpdateStatus().observe(getViewLifecycleOwner(), status -> {
            if (status != null) {
                Message.showShort(view, status);
            }
        });
    }

    private void setupLanguageSpinnerListener() {
        binding.spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String selectedLanguageCode;

                if (position == 0) {
                    selectedLanguageCode = "en";
                } else if (position == 1) {
                    selectedLanguageCode = "vi";
                } else {
                    return;
                }

                String currentCode = settingViewModel.getCurrentUser().getValue() != null &&
                        settingViewModel.getCurrentUser().getValue().getUserSetting() != null &&
                        settingViewModel.getCurrentUser().getValue().getUserSetting().getLanguage() != null ?
                        settingViewModel.getCurrentUser().getValue().getUserSetting().getLanguage().getCode() : "en";

                if (!selectedLanguageCode.equals(currentCode)) {
                    settingViewModel.updateLanguageSetting(selectedLanguageCode);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    private void showUpdateDialog(String dialogTitle, String fieldName, String currentValue, boolean requiresCurrentPassword) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.dialog_update_user_info, null);

        TextView titleTextView = dialogView.findViewById(R.id.dialog_title);
        EditText inputEditText = dialogView.findViewById(R.id.dialog_input);

        EditText currentPasswordEditText = dialogView.findViewById(R.id.et_current_password);

        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnConfirm = dialogView.findViewById(R.id.btn_confirm);

        titleTextView.setText("Enter new " + dialogTitle.toLowerCase());
        inputEditText.setText(currentValue);

        if (requiresCurrentPassword) {
            currentPasswordEditText.setVisibility(View.VISIBLE);
            currentPasswordEditText.setHint("Current Password (Required)");

            if (fieldName.equals("password")) {
                inputEditText.setText("");
                inputEditText.setHint("Enter new password");
                inputEditText.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                        android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            } else if (fieldName.equals("email")) {
                inputEditText.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            }
        } else {
            currentPasswordEditText.setVisibility(View.GONE);
        }

        AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> alertDialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String newValue = inputEditText.getText().toString().trim();
            String currentPassword = currentPasswordEditText.getText().toString().trim();

            if (newValue.isEmpty()) {
                Message.showShort(v, "Value cannot be empty!");
                return;
            }

            if (requiresCurrentPassword && currentPassword.isEmpty()) {
                Message.showShort(v, "Current password is required for security!");
                return;
            }

            settingViewModel.updateProfileField(fieldName, newValue, currentPassword);

            alertDialog.dismiss();
        });

        alertDialog.show();
    }
}