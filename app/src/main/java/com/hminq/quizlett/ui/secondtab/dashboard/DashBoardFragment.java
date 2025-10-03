package com.hminq.quizlett.ui.secondtab.dashboard;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hminq.quizlett.R;
import com.hminq.quizlett.databinding.FragmentDashBoardBinding;

public class DashBoardFragment extends Fragment {
    private FragmentDashBoardBinding binding;
    private NavController navController;

    public DashBoardFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        navController = NavHostFragment.findNavController(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDashBoardBinding.inflate(inflater, container, false);
        return  binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnToQuestionManage.setOnClickListener(l -> navController.navigate(R.id.action_dashBoardFragment_to_questionListFragment));
        binding.btnToLessonManage.setOnClickListener(l -> navController.navigate(R.id.action_dashBoardFragment_to_createLessonFragment));
    }
}