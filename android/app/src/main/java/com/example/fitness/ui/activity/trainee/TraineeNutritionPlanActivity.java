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
import com.example.fitness.databinding.ActivityTraineeNutritionPlanBinding;
import com.example.fitness.ui.adapter.NutritionPlanTabAdapter;
import com.example.fitness.ui.dialog.CreateNutritionPlanDialogFragment;
import com.example.fitness.ui.viewmodel.TraineeNutritionPlanViewModel;
import com.google.android.material.tabs.TabLayoutMediator;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TraineeNutritionPlanActivity extends AppCompatActivity {

    private ActivityTraineeNutritionPlanBinding binding;
    private TraineeNutritionPlanViewModel viewModel;
    private NutritionPlanTabAdapter tabAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        binding = ActivityTraineeNutritionPlanBinding.inflate(getLayoutInflater());
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
        viewModel = new ViewModelProvider(this).get(TraineeNutritionPlanViewModel.class);
    }

    private void setupViewPager() {
        tabAdapter = new NutritionPlanTabAdapter(this);
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
        
        binding.fabAddNutritionPlan.setOnClickListener(v -> showCreateNutritionPlanDialog());
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
                Toast.makeText(this, "Nutrition plan created successfully", Toast.LENGTH_SHORT).show();
                viewModel.clearCreatedPlan();
                
                // Refresh the fragments explicitly
                if (tabAdapter != null) {
                    tabAdapter.refreshFragments();
                }
            }
        });
    }

    private void showCreateNutritionPlanDialog() {
        CreateNutritionPlanDialogFragment dialog = new CreateNutritionPlanDialogFragment();
        dialog.setOnNutritionPlanCreateListener((name, description) -> {
            viewModel.createNutritionPlan(name, description);
        });
        dialog.show(getSupportFragmentManager(), "CreateNutritionPlanDialog");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning from activities
        if (tabAdapter != null) {
            tabAdapter.refreshFragments();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}