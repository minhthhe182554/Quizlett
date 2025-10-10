package com.hminq.quizlett.ui.firsttab.home;

import static com.hminq.quizlett.constants.AppMessage.WELCOME_BACK;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.hminq.quizlett.R;
import com.hminq.quizlett.databinding.FragmentHomeBinding;
import com.hminq.quizlett.ui.SharedViewModel;
import com.hminq.quizlett.utils.ImageLoader;
import com.hminq.quizlett.utils.Message;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment {
    private static final String TAG = "FRAGMENT_HOME";
    private FragmentHomeBinding binding;
    private SharedViewModel sharedViewModel;
    private NavController navController;
    private ImageView ivProfileImg;
    private SearchView svSearch;

    public HomeFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        navController = NavHostFragment.findNavController(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        bindViews();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedViewModel.getCurrentUserLiveData().observe(getViewLifecycleOwner(), currentUser -> {
            if (currentUser != null) {
                Message.showShort(view, getString(WELCOME_BACK) + " " + currentUser.getFullname());

                sharedViewModel.loadUserImage(currentUser);
            }
        });

        sharedViewModel.getProfileImgUrlLiveData().observe(getViewLifecycleOwner(), profileImgUrl -> {
            if (profileImgUrl != null) {
                Log.d(TAG, "Loading profile img: " + profileImgUrl);
                ImageLoader.loadImage(ivProfileImg, profileImgUrl);
            }
        });

        ivProfileImg.setOnClickListener(l -> navController.navigate(R.id.action_homeFragment_to_settingFragment));
    }

    private void bindViews() {
        ivProfileImg = binding.profileImage;
        svSearch = binding.svLesson;
    }
}