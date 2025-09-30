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
    private ForgotPasswordVieModel forgotPasswordVieModel;
    private NavController navController;
    private Button btnBack, btnResetPassword;
    private EditText etEmail;

    public ForgotPasswordFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        forgotPasswordVieModel = new ViewModelProvider(this).get(ForgotPasswordVieModel.class);

        navController = NavHostFragment.findNavController(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false);
        bindViews();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnBack.setOnClickListener(l -> navController.popBackStack());

        btnResetPassword.setOnClickListener(l -> {
            forgotPasswordVieModel.resetPassword(etEmail.getText().toString());
        });

        forgotPasswordVieModel.getValidationErrorLiveData().observe(getViewLifecycleOwner(), exception -> {
            if (exception.getField() == EMAIL) {
                etEmail.setError(exception.getMessage());
            }
        });

        forgotPasswordVieModel.getResetErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            Message.showShort(view, getString(EMAIL_SENT_ERROR));
        });

        forgotPasswordVieModel.getResetSuccessLiveData().observe(getViewLifecycleOwner(), success -> {
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