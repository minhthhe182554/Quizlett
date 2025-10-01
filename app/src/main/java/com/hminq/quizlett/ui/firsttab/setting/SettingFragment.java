package com.hminq.quizlett.ui.firsttab.setting;

import static com.hminq.quizlett.constants.AppMessage.DELETE_ACCOUNT_ERROR;
import static com.hminq.quizlett.constants.AppMessage.SIGNOUT_ERROR;

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

import com.hminq.quizlett.databinding.FragmentSettingBinding;
import com.hminq.quizlett.ui.SharedViewModel;
import com.hminq.quizlett.utils.Message;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingFragment extends Fragment {
    private FragmentSettingBinding binding;
    private SharedViewModel sharedViewModel;
    private SettingViewModel settingViewModel;
    private NavController navController;
    private Button btnBack, btnSignOut, btnDeleteAccount;

    public SettingFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        settingViewModel = new ViewModelProvider(this).get(SettingViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        binding = FragmentSettingBinding.inflate(inflater, container, false);
        bindViews();

        // get NavController
        navController = NavHostFragment.findNavController(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnBack.setOnClickListener(l -> navController.popBackStack());

        btnSignOut.setOnClickListener(l -> sharedViewModel.signOut());
        btnDeleteAccount.setOnClickListener(l -> sharedViewModel.deleteAccount());

        sharedViewModel.getSignOutErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            Message.showShort(view, getString(SIGNOUT_ERROR));
        });

        sharedViewModel.getDeleteErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            Message.showShort(view, getString(DELETE_ACCOUNT_ERROR));
        });
    }

    private void bindViews() {
        btnBack = binding.btnBack;
        btnSignOut = binding.btnSignOut;
        btnDeleteAccount = binding.btnDelete;
    }
}