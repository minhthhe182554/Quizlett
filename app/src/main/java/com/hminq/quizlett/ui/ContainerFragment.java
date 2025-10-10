package com.hminq.quizlett.ui;
import static com.hminq.quizlett.constants.ResourceConstant.TAB1_ICON;
import static com.hminq.quizlett.constants.ResourceConstant.TAB2_ICON;
import static com.hminq.quizlett.constants.ResourceConstant.TAB3_ICON;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.hminq.quizlett.R;
import com.hminq.quizlett.databinding.FragmentContainerBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ContainerFragment extends Fragment {
    private FragmentContainerBinding binding;
    private SharedViewModel sharedViewModel;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ViewPagerAdapter viewPagerAdapter;

    public ContainerFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentContainerBinding.inflate(inflater, container, false);

        viewPager = binding.viewPager;
        tabLayout = binding.tabLayout;

        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            //set tab icon
            switch (position) {
                case 0:
                    tab.setIcon(TAB1_ICON); // tab1 always selected by default
                    break;
                case 1:
                    tab.setIcon(TAB2_ICON);
                    break;
                case 2:
                    tab.setIcon(TAB3_ICON);
                    break;
                default:
                    break;
            }
        }).attach();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}