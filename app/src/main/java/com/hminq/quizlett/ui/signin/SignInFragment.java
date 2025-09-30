package com.hminq.quizlett.ui.signin;

import static com.hminq.quizlett.constants.AppMessage.SIGNIN_ERROR;

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
import com.hminq.quizlett.databinding.FragmentSignInBinding;
import com.hminq.quizlett.utils.Message;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SignInFragment extends Fragment {
    private static final String TAG = "FRAGMENT_SIGNIN";
    private FragmentSignInBinding binding;
    private SignInViewModel signInViewModel;
    private NavController navController;
    private Button btnSignIn, btnBack;
    private EditText etEmail, etPassword;
    private TextView tvLinkToSignUp, tvForgotPassword;

    public SignInFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        signInViewModel = new ViewModelProvider(requireActivity()).get(SignInViewModel.class);
        navController = NavHostFragment.findNavController(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSignInBinding.inflate(inflater, container, false);
        bindViews();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvLinkToSignUp.setOnClickListener(l -> navController.navigate(R.id.action_signInFragment_to_signUpFragment));
        btnBack.setOnClickListener(l -> navController.navigate(R.id.action_signInFragment_to_welcomeFragment));

        btnSignIn.setOnClickListener(l -> {
            signInViewModel.signIn(
                    etEmail.getText().toString(),
                    etPassword.getText().toString()
            );
        });

        tvForgotPassword.setOnClickListener(l -> {
            navController.navigate(R.id.action_signInFragment_to_forgotPasswordFragment);
        });

        signInViewModel.getValidationExceptionLiveData().observe(getViewLifecycleOwner(), exception -> {
            switch (exception.getField()) {
                case EMAIL:
                    etEmail.setError(exception.getMessage());
                    break;
                case PASSWORD:
                    etPassword.setError(exception.getMessage());
                    break;
            }
        });

        signInViewModel.getSignInErrorLiveData().observe(getViewLifecycleOwner(), throwable -> {
            if (throwable != null) {
                Message.showShort(view, getString(SIGNIN_ERROR));
            }
        });

        signInViewModel.getIsUserSignedInLiveData().observe(getViewLifecycleOwner(), isUserSignedIn -> {
            if (isUserSignedIn) {
                navController.navigate(R.id.action_signInFragment_to_homeFragment);
            }
            else {
                Log.d(TAG, "User somehow not signin");
            }
        });
    }

    private void bindViews() {
        btnSignIn = binding.btnSignIn;
        btnBack = binding.btnBack;
        etEmail = binding.etEmail;
        etPassword = binding.etPassword;
        tvLinkToSignUp = binding.linkToSignUp;
        tvForgotPassword = binding.tvForgotPassword;
    }
}