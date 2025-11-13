package com.hminq.quizlett.ui.firsttab.home;

import static com.hminq.quizlett.constants.AppMessage.WELCOME_BACK;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hminq.quizlett.R;
import com.hminq.quizlett.data.remote.model.Lesson;
import com.hminq.quizlett.data.remote.model.LessonCategory;
import com.hminq.quizlett.databinding.FragmentHomeBinding;
import com.hminq.quizlett.ui.SharedViewModel;
import com.hminq.quizlett.ui.firsttab.home.adapter.HomeLessonAdapter;
import com.hminq.quizlett.utils.ImageLoader;
import com.hminq.quizlett.utils.Message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment {
    private static final String TAG = "FRAGMENT_HOME";
    private FragmentHomeBinding binding;
    private SharedViewModel sharedViewModel;
    private HomeViewModel homeViewModel;
    private NavController navController;
    private ImageView ivProfileImg;
    private SearchView svSearch;
    private ProgressBar progressBar;
    private RecyclerView rvTopLessons;
    private TextView tvTopLessonsEmpty;
    private LinearLayout categoryContainer;
    private HomeLessonAdapter topLessonsAdapter;
    private Map<LessonCategory, HomeLessonAdapter> categoryAdapters;
    private Map<LessonCategory, RecyclerView> categoryRecyclerViews;

    public HomeFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        navController = NavHostFragment.findNavController(this);

        categoryAdapters = new HashMap<>();
        categoryRecyclerViews = new HashMap<>();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        bindViews();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupTopLessonsRecyclerView();
        setupSearchView();
        observeSharedViewModel(view);
        observeHomeViewModel();

        // Load ALL lessons from all users
        homeViewModel.loadAllLessons();
    }

    private void setupTopLessonsRecyclerView() {
        rvTopLessons.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false));

        topLessonsAdapter = new HomeLessonAdapter(this::onLessonClick); //fix: Simplified
        rvTopLessons.setAdapter(topLessonsAdapter);
    }

    private void setupSearchView() {
        svSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                homeViewModel.searchLessons(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.trim().isEmpty()) {
                    // Show all lessons when search is cleared
                    homeViewModel.loadAllLessons();
                    // Show categories
                    if (categoryContainer != null) {
                        categoryContainer.setVisibility(View.VISIBLE);
                    }
                } else {
                    homeViewModel.searchLessons(newText);
                }
                return true;
            }
        });
    }

    private void observeSharedViewModel(View view) {
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
    }

    private void observeHomeViewModel() {
        homeViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (progressBar != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        homeViewModel.getAllLessons().observe(getViewLifecycleOwner(), lessons -> {
            if (lessons != null) {
                topLessonsAdapter.setLessons(lessons);
                updateEmptyView(tvTopLessonsEmpty, rvTopLessons, lessons.isEmpty());
            }
        });

        homeViewModel.getLessonsByCategory().observe(getViewLifecycleOwner(), categoryMap -> {
            if (categoryMap != null) {
                createCategorySections(categoryMap);
            }
        });

        homeViewModel.getSearchResults().observe(getViewLifecycleOwner(), searchResults -> {
            if (searchResults != null) {
                topLessonsAdapter.setLessons(searchResults);
                updateEmptyView(tvTopLessonsEmpty, rvTopLessons, searchResults.isEmpty());

                // Hide categories during search
                if (categoryContainer != null) {
                    categoryContainer.setVisibility(View.GONE);
                }
            }
        });

        homeViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Log.e(TAG, "Error: " + error);
                Message.showShort(getView(), "Error loading lessons: " + error);
                homeViewModel.clearError();
            }
        });
    }

    private void createCategorySections(Map<LessonCategory, List<Lesson>> categoryMap) {
        if (categoryContainer == null) return;

        // Clear existing views
        categoryContainer.removeAllViews();
        categoryAdapters.clear();
        categoryRecyclerViews.clear();

        for (Map.Entry<LessonCategory, List<Lesson>> entry : categoryMap.entrySet()) {
            LessonCategory category = entry.getKey();
            List<Lesson> lessons = entry.getValue();

            // Skip empty categories
            if (lessons.isEmpty()) {
                continue;
            }

            // Inflate category section
            View sectionView = getLayoutInflater().inflate(
                    R.layout.item_category_section_home, categoryContainer, false);

            TextView tvCategoryTitle = sectionView.findViewById(R.id.tvCategoryTitle);
            RecyclerView rvCategory = sectionView.findViewById(R.id.rvCategoryLessons);
            TextView tvEmptyCategory = sectionView.findViewById(R.id.tvEmptyCategory);

            // Set category title
            tvCategoryTitle.setText(category.name());

            // Setup RecyclerView
            rvCategory.setLayoutManager(new LinearLayoutManager(getContext(),
                    LinearLayoutManager.HORIZONTAL, false));

            HomeLessonAdapter adapter = new HomeLessonAdapter(this::onLessonClick); //fix: Simplified
            rvCategory.setAdapter(adapter);
            adapter.setLessons(lessons);

            // Update empty view
            updateEmptyView(tvEmptyCategory, rvCategory, lessons.isEmpty());

            // Store references
            categoryAdapters.put(category, adapter);
            categoryRecyclerViews.put(category, rvCategory);

            // Add to container
            categoryContainer.addView(sectionView);
        }
    }

    private void onLessonClick(Lesson lesson) {
        // Update visit count
        homeViewModel.updateVisitCount(lesson.getLessonId());

        // Navigate to lesson detail
        Log.d(TAG, "Clicked lesson: " + lesson.getTitle());
        Message.showShort(getView(), "Opening: " + lesson.getTitle());

        // navigation to lesson detail
        Bundle lessonClickedData = new Bundle();

        lessonClickedData.putSerializable("lesson", lesson);
        navController.navigate(R.id.action_homeFragment_to_lessonDetailFragment3, lessonClickedData);
    }

    private void updateEmptyView(TextView emptyView, RecyclerView recyclerView, boolean isEmpty) {
        if (emptyView != null && recyclerView != null) {
            emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    private void bindViews() {
        // These views already exist in your original layout
        ivProfileImg = binding.profileImage;
        svSearch = binding.svLesson;
        //set hint text color
        EditText searchEditText = svSearch.findViewById(androidx.appcompat.R.id.search_src_text);
        if (searchEditText != null) {
            searchEditText.setHintTextColor(Color.parseColor("#80FFFFFF"));
        }

        // set icon color
        ImageView searchIcon = svSearch.findViewById(androidx.appcompat.R.id.search_mag_icon);

        if (searchIcon != null) {
            Log.d(TAG, "ICON IS NOT NULL");
            searchIcon.setColorFilter(
                    Color.parseColor("#80FFFFFF"),
                    android.graphics.PorterDuff.Mode.SRC_IN
            );
        }
        // New views for lesson display
        progressBar = binding.progressBar;
        rvTopLessons = binding.rvTopLessons;
        tvTopLessonsEmpty = binding.tvTopLessonsEmpty;
        categoryContainer = binding.categoryContainer;

        // Profile image click listener
        ivProfileImg.setOnClickListener(l ->
                navController.navigate(R.id.action_homeFragment_to_settingFragment));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        categoryAdapters.clear();
        categoryRecyclerViews.clear();
        binding = null;
    }
}
