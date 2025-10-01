package com.hminq.quizlett.ui.forgotpassword;

import static com.hminq.quizlett.constants.AppMessage.EMAIL_SENT_ERROR;
import static com.hminq.quizlett.constants.AppMessage.EMAIL_SENT_SUCCESS;
import static com.hminq.quizlett.exceptions.ValidationException.Field.EMAIL;

import android.os.Bundle;

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
import com.hminq.quizlett.databinding.FragmentForgotPasswordBinding;
import com.hminq.quizlett.utils.Message;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ForgotPasswordFragment extends Fragment {
    private static final String TAG = "FRAGMENT_FORGOT_PASSWORD";
    private FragmentForgotPasswordBinding binding;
    private ForgotPasswordViewModel forgotPasswordViewModel;
    private NavController navController;
    private Button btnBack, btnResetPassword;
    private EditText etEmail;

    public ForgotPasswordFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        forgotPasswordViewModel = new ViewModelProvider(this).get(ForgotPasswordViewModel.class);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false);
        bindViews();
        // get NavController
        navController = NavHostFragment.findNavController(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnBack.setOnClickListener(l -> navController.popBackStack());

        btnResetPassword.setOnClickListener(l -> {
            forgotPasswordViewModel.resetPassword(etEmail.getText().toString());
        });

        forgotPasswordViewModel.getValidationErrorLiveData().observe(getViewLifecycleOwner(), exception -> {
            if (exception.getField() == EMAIL) {
                etEmail.setError(exception.getMessage());
            }
        });

        forgotPasswordViewModel.getResetErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            Message.showShort(view, getString(EMAIL_SENT_ERROR));
        });

        forgotPasswordViewModel.getResetSuccessLiveData().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Message.showShort(view, getString(EMAIL_SENT_SUCCESS));
                navController.popBackStack();
            }
        });
    }

    private void bindViews() {
        btnBack = binding.btnBack;
        btnResetPassword = binding.btnReset;
        etEmail = binding.etEmail;
    }
}