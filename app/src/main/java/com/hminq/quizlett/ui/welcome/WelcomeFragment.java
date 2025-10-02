package com.hminq.quizlett.ui.welcome;

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
import android.widget.TextView;

import com.hminq.quizlett.R;
import com.hminq.quizlett.databinding.FragmentWelcomeBinding;
import com.hminq.quizlett.ui.SharedViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WelcomeFragment extends Fragment {
    private static final String TAG = "FRAGMENT_WELCOME";
    private FragmentWelcomeBinding binding;
    private NavController navController;
    private Button btnSignUp;
    private TextView tvLinkToSignIn;

    public WelcomeFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d(TAG, "onCreateView");
        binding = FragmentWelcomeBinding.inflate(inflater, container, false);
        bindViews();
        // get NavController
        navController = NavHostFragment.findNavController(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnSignUp.setOnClickListener(l -> navController.navigate(R.id.action_welcomeFragment_to_signUpFragment));
        tvLinkToSignIn.setOnClickListener(l -> navController.navigate(R.id.action_welcomeFragment_to_signInFragment));
    }

    private void bindViews() {
        btnSignUp = binding.btnSignUp;
        tvLinkToSignIn = binding.linkToSignIn;
    }
}