package com.hminq.quizlett.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
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

import com.google.firebase.auth.FirebaseAuth;
import com.hminq.quizlett.R;
import com.hminq.quizlett.databinding.FragmentWelcomeBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WelcomeFragment extends Fragment {
    private static final String TAG = "FRAGMENT_WELCOME";
    private FragmentWelcomeBinding binding;
    private SharedViewModel sharedViewModel;
    private NavController navController;
    private Button btnSignUp;
    private TextView tvLinkToSignIn;

    public WelcomeFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        // get SharedViewModel with MainActivity scope
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // get NavController
        navController = NavHostFragment.findNavController(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d(TAG, "onCreateView");
        binding = FragmentWelcomeBinding.inflate(inflater, container, false);
        bindViews();
        FirebaseAuth.getInstance().signOut();
        // check if any user signed in
        checkIfUserSignedIn();

        btnSignUp.setOnClickListener(l -> navController.navigate(R.id.action_welcomeFragment_to_signUpFragment));
        tvLinkToSignIn.setOnClickListener(l -> navController.navigate(R.id.action_welcomeFragment_to_signInFragment));
        return binding.getRoot();
    }

    private void checkIfUserSignedIn() {
        sharedViewModel.getCurrentUser();

        sharedViewModel.getCurrentUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                Log.d(TAG, "Current user: " + user.getFullname());
                navController.navigate(R.id.action_welcomeFragment_to_homeFragment);
            }
            else {
                Log.d(TAG, "No user signed in.");
            }
        });

        sharedViewModel.getAuthErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Log.d(TAG, error.getMessage());
            }
        });
    }

    private void bindViews() {
        btnSignUp = binding.btnSignUp;
        tvLinkToSignIn = binding.linkToSignIn;
    }
}