package com.hminq.quizlett.ui.home;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hminq.quizlett.R;
import com.hminq.quizlett.databinding.FragmentHomeBinding; // Added View Binding import
import com.hminq.quizlett.ui.SharedViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment {
    private static final String TAG = "FRAGMENT_HOME";
    private SharedViewModel sharedViewModel;
    private FragmentHomeBinding binding; // Added binding variable
    private NavController navController;

    public HomeFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment using View Binding
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        sharedViewModel.getCurrentUserLiveData().observe(getViewLifecycleOwner(), currentUser -> {
            if (currentUser != null) {
                Log.d(TAG, "Current user: " + currentUser.getEmail());
                // You might want to update some UI elements here with user info if needed
                // For example: binding.tvHomeTitle.setText("Welcome, " + currentUser.getFullname());
            }
        });

        // Set click listener for the Manage Questions button
        binding.btnManageQuestionsHome.setOnClickListener(v -> {
            // TODO: Ensure R.id.action_homeFragment_to_questionListFragment exists in your nav_graph.xml
            navController.navigate(R.id.action_homeFragment_to_questionListFragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Important to prevent memory leaks with View Binding
    }
}