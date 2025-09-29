package com.hminq.quizlett;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.hminq.quizlett.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(R.layout.activity_main);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                                                            .findFragmentById(binding.navHostFragment.getId());

        // get NavController at MainActivity
        navController = navHostFragment.getNavController();
    }
}