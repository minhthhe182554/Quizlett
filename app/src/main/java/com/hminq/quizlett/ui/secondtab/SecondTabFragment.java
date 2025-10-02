package com.hminq.quizlett.ui.secondtab;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation; // Import đầy đủ

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.hminq.quizlett.R;
import com.hminq.quizlett.databinding.FragmentSecondTabBinding;

public class SecondTabFragment extends Fragment {

    private FragmentSecondTabBinding binding;


    public SecondTabFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSecondTabBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = Navigation.findNavController(view);

        binding.btnGoToManageQuestions.setOnClickListener(v -> {
            navController.navigate(R.id.action_secondTabFragment_to_questionListFragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
