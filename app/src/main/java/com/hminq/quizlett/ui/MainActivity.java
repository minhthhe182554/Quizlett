package com.hminq.quizlett.ui;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.hminq.quizlett.R;
import com.hminq.quizlett.data.remote.model.User;
import com.hminq.quizlett.databinding.ActivityMainBinding;
import com.hminq.quizlett.utils.Message;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MAIN_ACTIVITY";
    private SharedViewModel sharedViewModel;
    private NavController navController;
    private NavOptions navigateToAndClearStackOption;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.navHostFragment);

        navController = navHostFragment.getNavController();
        navigateToAndClearStackOption = new NavOptions.Builder()
                .setPopUpTo(R.id.app_nav_graph, true)
                .build();
        checkIfUserSignedIn();
        checkIfUserSignedOut();
        setContentView(binding.getRoot());
    }

    private void checkIfUserSignedIn() {
        sharedViewModel.getCurrentUser();

        sharedViewModel.getCurrentUserLiveData().observe(this, new Observer<>() {
            @Override
            public void onChanged(User user) {
                if (user != null) {
                    Log.d(TAG, "Current user: " + user.getFullname());
                    navController.navigate(
                            R.id.containerFragment,
                            null,
                            navigateToAndClearStackOption
                    );

                    sharedViewModel.getCurrentUserLiveData().removeObserver(this);
                } else {
                    Log.d(TAG, "User is null.");
                }
            }
        });

        sharedViewModel.getErrorLiveData().observe(this, error -> {
            if (error != null) {
                Log.d(TAG, error.getMessage());
                // Don't show error message on startup if user not logged in
            }
        });
    }

    private void checkIfUserSignedOut() {
        sharedViewModel.getSignOutSuccessLiveData().observe(this, success -> {
            if (success) {
                Log.d(TAG, "Sign out success: ");
                navController.navigate(
                        R.id.welcomeFragment,
                        null,
                        navigateToAndClearStackOption
                );
            }
        });

        sharedViewModel.getSignOutErrorLiveData().observe(this, error -> {
            Log.d(TAG, "Sign out error: " + error.getMessage());
        });
    }
}