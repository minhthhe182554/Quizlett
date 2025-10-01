package com.hminq.quizlett.ui.signup;

import static com.hminq.quizlett.constants.AppMessage.SIGNUP_ERROR;
import static com.hminq.quizlett.constants.AppMessage.SIGNUP_SUCCESS;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.hminq.quizlett.R;
import com.hminq.quizlett.databinding.FragmentSignUpBinding;
import com.hminq.quizlett.utils.Message;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SignUpFragment extends Fragment {
    private static final String TAG = "FRAGMENT_SIGNUP";
    private FragmentSignUpBinding binding;
    private SignUpViewModel signUpViewModel;
    private NavController navController;
    private EditText etEmail, etPassword, etFullname;
    private TextView tvSignIn;
    private Button btnSignUp, btnBack;

    public SignUpFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get SignUpViewModel with this Fragment scope
        signUpViewModel = new ViewModelProvider(this).get(SignUpViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSignUpBinding.inflate(inflater, container, false);
        bindViews();

        // get NavController
        navController = NavHostFragment.findNavController(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // click btn Sign Up
        btnSignUp.setOnClickListener(l -> signUpViewModel.signUp(
                etEmail.getText().toString(),
                etPassword.getText().toString(),
                etFullname.getText().toString())
        );

        // click link to navigate to Sign in screen
        tvSignIn.setOnClickListener(l -> navController.navigate(R.id.action_signUpFragment_to_signInFragment));

        // click back btn
        btnBack.setOnClickListener(l -> navController.navigate(R.id.action_signUpFragment_to_welcomeFragment));

        // Observe for Validation Errors
        signUpViewModel.getValidationExceptionLiveData().observe(getViewLifecycleOwner(), exception -> {
            switch (exception.getField()) {
                case EMAIL:
                    etEmail.setError(exception.getMessage());
                    break;
                case PASSWORD:
                    etPassword.setError(exception.getMessage());
                    break;
                case FULLNAME:
                    etFullname.setError(exception.getMessage());
                    break;
            }
        });

        signUpViewModel.getSignUpErrorLiveData().observe(getViewLifecycleOwner(), throwable -> {
            if (throwable != null) {
                Log.d(TAG, "Sign up failed, " + throwable.getMessage());
                Message.showShort(view, getString(SIGNUP_ERROR));
            }
        });

        signUpViewModel.getIsUserSignedUpLiveData().observe(getViewLifecycleOwner(), isUserSignedUp -> {
            if (isUserSignedUp) {
                // navigate to home
                Message.showShort(view, getString(SIGNUP_SUCCESS));
                navController.navigate(R.id.action_signUpFragment_to_containerFragment);
            }
        });
    }

    private void bindViews() {
        etEmail = binding.etEmail;
        etPassword = binding.etPassword;
        etFullname = binding.etFullname;
        btnSignUp = binding.btnSignUp;
        btnBack = binding.btnBack;
        tvSignIn = binding.linkToSignIn;
    }
}