package com.hminq.quizlett.ui.firsttab.setting;

import static com.hminq.quizlett.constants.AppMessage.DELETE_ACCOUNT_ERROR;
import static com.hminq.quizlett.constants.AppMessage.SIGNOUT_ERROR;

import android.app.AlertDialog;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hminq.quizlett.R;
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
    private Button btnBack, btnSignOut, btnDeleteAccount;
    private ImageView userAvatar;
    private TextView profileDisplayName, profileFullName, profileEmail, profilePassword;
    private Spinner spinnerLanguage;
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

        // Inflate the layout for this fragment
        binding = FragmentSettingBinding.inflate(inflater, container, false);
        bindViews();

        // get NavController
        navController = NavHostFragment.findNavController(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnBack.setOnClickListener(l -> navController.popBackStack());

        btnSignOut.setOnClickListener(l -> sharedViewModel.signOut());
//        btnDeleteAccount.setOnClickListener(l -> sharedViewModel.deleteAccount());
        btnDeleteAccount.setOnClickListener(l -> {
            Message.showShort(view, "Đừng bấm vào chưa làm xong");
        });

        sharedViewModel.getSignOutErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            Message.showShort(view, getString(SIGNOUT_ERROR));
        });

        sharedViewModel.getDeleteErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            Message.showShort(view, getString(DELETE_ACCOUNT_ERROR));
        });

        settingViewModel.getProfileImageUrl().observe(getViewLifecycleOwner(), imageUrl -> {
            if (imageUrl != null && userAvatar != null) {
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.welcome_img)
                        .error(R.drawable.welcome_img)
                        .into(userAvatar);
            }
        });

        binding.userAvatar.setOnClickListener(l -> {
            imagePickerLauncher.launch("image/*");
        });

        profileFullName.setOnClickListener(l -> {
            String currentValue = profileFullName.getText().toString();
            showUpdateDialog("Full name", "fullname", currentValue, false);
        });

        profilePassword.setOnClickListener(l -> {
            showUpdateDialog("New Password", "password", "", true);
        });

        settingViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                profileDisplayName.setText(user.getFullname());

                profileFullName.setText(user.getFullname());
                profileEmail.setText(user.getEmail());
                profilePassword.setText(user.getPassword());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private void bindViews() {
        btnBack = binding.btnBack;
        btnSignOut = binding.btnSignOut;
        btnDeleteAccount = binding.btnDelete;

        userAvatar = binding.userAvatar;
        profileDisplayName = binding.profileDisplayName;
        profileFullName = binding.profileFullName;
        profileEmail = binding.profileEmail;
        profilePassword = binding.profilePassword;

        spinnerLanguage = binding.spinnerLanguage;
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
                inputEditText.setText(""); // Chưa hiển thị ******
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