package com.example.fitness.ui.activity.trainee;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.fitness.R;
import com.example.fitness.databinding.ActivityTraineeWorkoutPlanBinding;
import com.example.fitness.ui.adapter.WorkoutPlanTabAdapter;
import com.example.fitness.ui.dialog.CreateWorkoutPlanDialogFragment;
import com.example.fitness.ui.viewmodel.WorkoutPlanViewModel;
import com.google.android.material.tabs.TabLayoutMediator;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TraineeWorkoutPlanActivity extends AppCompatActivity {

    private ActivityTraineeWorkoutPlanBinding binding;
    private WorkoutPlanViewModel viewModel;
    private WorkoutPlanTabAdapter tabAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        binding = ActivityTraineeWorkoutPlanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupViewModel();
        setupViewPager();
        setupListeners();
        observeViewModel();
    }

    private void initializeViews() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(WorkoutPlanViewModel.class);
        viewModel.loadUserWorkoutPlanAssignments();
    }

    private void setupViewPager() {
        tabAdapter = new WorkoutPlanTabAdapter(this);
        binding.viewPager.setAdapter(tabAdapter);
        
        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Active");
                            break;
                        case 1:
                            tab.setText("Completed");
                            break;
                    }
                }
        ).attach();
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        
        binding.fabAddWorkoutPlan.setOnClickListener(v -> showCreateWorkoutPlanDialog());
    }

    private void observeViewModel() {
        viewModel.isLoading.observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.error.observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                viewModel.clearError();
            }
        });

        viewModel.createdPlan.observe(this, createdPlan -> {
            if (createdPlan != null) {
                Toast.makeText(this, "Workout plan created successfully", Toast.LENGTH_SHORT).show();
                viewModel.clearCreatedPlan();
            }
        });
    }

    private void showCreateWorkoutPlanDialog() {
        CreateWorkoutPlanDialogFragment dialog = new CreateWorkoutPlanDialogFragment();
        dialog.setOnWorkoutPlanCreateListener((name, description, difficulty) -> {
            viewModel.createWorkoutPlan(name, description, difficulty);
        });
        dialog.show(getSupportFragmentManager(), "CreateWorkoutPlanDialog");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}