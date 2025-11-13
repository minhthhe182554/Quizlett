package com.hminq.quizlett.ui;
import static com.hminq.quizlett.constants.ResourceConstant.TAB1_ICON;
import static com.hminq.quizlett.constants.ResourceConstant.TAB2_ICON;
import static com.hminq.quizlett.constants.ResourceConstant.TAB3_ICON;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.hminq.quizlett.databinding.FragmentContainerBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ContainerFragment extends Fragment {
    private FragmentContainerBinding binding;
    private SharedViewModel sharedViewModel;
    private MainTabViewModel mainTabViewModel;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ViewPagerAdapter viewPagerAdapter;
    private ViewPager2.OnPageChangeCallback pageChangeCallback;

    public ContainerFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        mainTabViewModel = new ViewModelProvider(requireActivity()).get(MainTabViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentContainerBinding.inflate(inflater, container, false);

        viewPager = binding.viewPager;
        tabLayout = binding.tabLayout;

        viewPager.setUserInputEnabled(false);
        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);

        observeTabNavigation();
        registerPageChangeCallback();

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            //set tab icon
            switch (position) {
                case 0:
                    tab.setIcon(TAB1_ICON);
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

    @Override
    public void onDestroyView() {
        if (pageChangeCallback != null) {
            viewPager.unregisterOnPageChangeCallback(pageChangeCallback);
            pageChangeCallback = null;
        }
        binding = null;
        super.onDestroyView();
    }

    private void observeTabNavigation() {
        mainTabViewModel.getTargetTab().observe(getViewLifecycleOwner(), tabIndex -> {
            if (tabIndex == null) {
                return;
            }

            if (tabIndex >= 0 && tabIndex < viewPagerAdapter.getItemCount()) {
                if (viewPager.getCurrentItem() != tabIndex) {
                    viewPager.setCurrentItem(tabIndex, false);
                }
            }
            mainTabViewModel.clearTargetTab();
        });
    }

    private void registerPageChangeCallback() {
        pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position != MainTabViewModel.TAB_INDEX_THIRD) {
                    mainTabViewModel.clearLessonToSave();
                }
            }
        };
        viewPager.registerOnPageChangeCallback(pageChangeCallback);
    }
}