package com.hminq.quizlett.ui.secondtab.dashboard;

import static com.hminq.quizlett.constants.AppMessage.WELCOME_BACK;

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

import com.hminq.quizlett.R;
import com.hminq.quizlett.databinding.FragmentDashBoardBinding;
import com.hminq.quizlett.ui.SharedViewModel;
import com.hminq.quizlett.utils.ImageLoader;
import com.hminq.quizlett.utils.Message;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DashBoardFragment extends Fragment {
    private static final String TAG = "FRAGMENT_DASHBOARD";
    private FragmentDashBoardBinding binding;
    private NavController navController;
    private SharedViewModel sharedViewModel;
    private DashboardViewModel dashboardViewModel;

    public DashBoardFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        navController = NavHostFragment.findNavController(this);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDashBoardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnToQuestionManage.setOnClickListener(l -> navController.navigate(R.id.action_dashBoardFragment_to_questionListFragment));
        binding.btnToLessonManage.setOnClickListener(l -> navController.navigate(R.id.action_dashBoardFragment_to_lessonListFragment));

        sharedViewModel.getCurrentUserLiveData().observe(getViewLifecycleOwner(), currentUser -> {
            if (currentUser != null) {
                dashboardViewModel.loadData(currentUser.getUid());
                binding.tvUserFullname.setText(currentUser.getFullname());
            }
        });

        sharedViewModel.getProfileImgUrlLiveData().observe(getViewLifecycleOwner(), profileImgUrl -> {
            if (profileImgUrl != null) {
                Log.d(TAG, "Loading profile img: " + profileImgUrl);
                ImageLoader.loadImage(binding.ivUserImage, profileImgUrl);
            }
        });

        dashboardViewModel.getDashboardDataLive().observe(getViewLifecycleOwner(), dashboardData -> {
            binding.totalVisitCount.setText(String.valueOf(dashboardData.getVisitCount()));
            binding.totalLessonCount.setText(String.valueOf(dashboardData.getLessonCount()));
            binding.totalQuestionCount.setText(String.valueOf(dashboardData.getQuestionCount()));
        });

        dashboardViewModel.getErrorLive().observe(getViewLifecycleOwner(), error -> {
            Message.showShort(view, "Error when getting data.");
        });
    }
}