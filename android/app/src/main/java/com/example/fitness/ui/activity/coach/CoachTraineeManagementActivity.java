package com.example.fitness.ui.activity.coach;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.fitness.R;
import com.example.fitness.databinding.ActivityCoachTraineeManagementBinding;
import com.example.fitness.ui.activity.coach.AssignNutritionPlanActivity;
import com.example.fitness.ui.activity.coach.AssignWorkoutPlanActivity;
import com.example.fitness.ui.adapter.TraineeManagementPagerAdapter;
import com.example.fitness.ui.viewmodel.TraineeManagementViewModel;
import com.google.android.material.tabs.TabLayoutMediator;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CoachTraineeManagementActivity extends AppCompatActivity {
    
    private static final int REQUEST_ASSIGN_WORKOUT = 1001;
    private static final int REQUEST_ASSIGN_NUTRITION = 1002;
    
    private ActivityCoachTraineeManagementBinding binding;
    private TraineeManagementViewModel viewModel;
    private TraineeManagementPagerAdapter pagerAdapter;
    private String traineeId;
    private String traineeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        binding = ActivityCoachTraineeManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        getIntentData();
        setupToolbar();
        setupViewPager();
        setupViewModel();
        setupFABs();
    }
    
    private void getIntentData() {
        traineeId = getIntent().getStringExtra("TRAINEE_ID");
        traineeName = getIntent().getStringExtra("TRAINEE_NAME");
        
        if (traineeId == null || traineeName == null) {
            Toast.makeText(this, "Missing trainee information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }
    
    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Manage " + traineeName);
        }
    }
    
    private void setupViewPager() {
        pagerAdapter = new TraineeManagementPagerAdapter(this, traineeId);
        binding.viewPager.setAdapter(pagerAdapter);
        
        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Workout Plans");
                    break;
                case 1:
                    tab.setText("Nutrition Plans");
                    break;
                case 2:
                    tab.setText("Body Stats");
                    break;
            }
        }).attach();
        
        // Set up tab selection listener to show/hide appropriate FAB
        binding.tabLayout.addOnTabSelectedListener(new com.google.android.material.tabs.TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(com.google.android.material.tabs.TabLayout.Tab tab) {
                updateFABVisibility(tab.getPosition());
            }

            @Override
            public void onTabUnselected(com.google.android.material.tabs.TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(com.google.android.material.tabs.TabLayout.Tab tab) {}
        });
    }
    
    private void setupFABs() {
        binding.fabAssignWorkout.setOnClickListener(v -> {
            Intent intent = new Intent(this, AssignWorkoutPlanActivity.class);
            intent.putExtra("TRAINEE_ID", traineeId);
            intent.putExtra("TRAINEE_NAME", traineeName);
            startActivityForResult(intent, REQUEST_ASSIGN_WORKOUT);
        });
        
        binding.fabAssignNutrition.setOnClickListener(v -> {
            Intent intent = new Intent(this, AssignNutritionPlanActivity.class);
            intent.putExtra("TRAINEE_ID", traineeId);
            intent.putExtra("TRAINEE_NAME", traineeName);
            startActivityForResult(intent, REQUEST_ASSIGN_NUTRITION);
        });
        
        // Initially show workout FAB (first tab)
        updateFABVisibility(0);
    }
    
    private void updateFABVisibility(int tabPosition) {
        switch (tabPosition) {
            case 0: // Workout Plans
                binding.fabAssignWorkout.setVisibility(android.view.View.VISIBLE);
                binding.fabAssignNutrition.setVisibility(android.view.View.GONE);
                break;
            case 1: // Nutrition Plans
                binding.fabAssignWorkout.setVisibility(android.view.View.GONE);
                binding.fabAssignNutrition.setVisibility(android.view.View.VISIBLE);
                break;
            case 2: // Body Stats
                binding.fabAssignWorkout.setVisibility(android.view.View.GONE);
                binding.fabAssignNutrition.setVisibility(android.view.View.GONE);
                break;
        }
    }
    
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(TraineeManagementViewModel.class);
        viewModel.setTraineeId(traineeId);
        
        viewModel.isLoading.observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? android.view.View.VISIBLE : android.view.View.GONE);
        });
        
        viewModel.errorMessage.observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                viewModel.clearMessages();
            }
        });
        
        viewModel.successMessage.observe(this, success -> {
            if (success != null) {
                Toast.makeText(this, success, Toast.LENGTH_SHORT).show();
                viewModel.clearMessages();
            }
        });
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // Refresh data after successful assignment
            if (viewModel != null) {
                viewModel.setTraineeId(traineeId);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        // This will trigger the fragments to reload their data
        if (viewModel != null) {
            viewModel.setTraineeId(traineeId);
        }
    }
}