package com.example.fitness.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.fitness.databinding.ActivityMainBinding;
import com.example.fitness.ui.activity.coach.CoachWorkoutPlanActivity;
import com.example.fitness.ui.activity.coach.CoachNutritionPlanActivity;
import com.example.fitness.ui.activity.coach.CoachTraineeActivity;
import com.example.fitness.ui.activity.trainee.TraineeCoachActivity;
import com.example.fitness.ui.activity.trainee.TraineeExercisePlanActivity;
import com.example.fitness.ui.activity.trainee.TraineeNutritionPlanActivity;
import com.example.fitness.ui.activity.trainee.TraineeStatsActivity;
import com.example.fitness.ui.viewmodel.MainViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    
    private MainViewModel mainViewModel;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        // Initialize ViewBinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize ViewModel
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        
        setupClickListeners();
        observeViewModel();
    }

    private void setupClickListeners() {
        binding.btnLogout.setOnClickListener(v -> {
            mainViewModel.logout();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        
        setupCoachActivityClickListeners();
        setupTraineeActivityClickListeners();
    }
    
    private void setupCoachActivityClickListeners() {
        binding.btnCoachTrainee.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CoachTraineeActivity.class);
            startActivity(intent);
        });
        
        binding.btnCoachExercisePlan.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CoachWorkoutPlanActivity.class);
            startActivity(intent);
        });
        
        binding.btnCoachNutritionPlan.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CoachNutritionPlanActivity.class);
            startActivity(intent);
        });
    }
    
    private void setupTraineeActivityClickListeners() {
        binding.btnTraineeCoach.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TraineeCoachActivity.class);
            startActivity(intent);
        });
        
        binding.btnTraineeExercisePlan.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TraineeExercisePlanActivity.class);
            startActivity(intent);
        });
        
        binding.btnTraineeNutritionPlan.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TraineeNutritionPlanActivity.class);
            startActivity(intent);
        });
        
        binding.btnTraineeStats.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TraineeStatsActivity.class);
            startActivity(intent);
        });
    }

    private void observeViewModel() {
        // Observe welcome message
        mainViewModel.welcomeMessage.observe(this, welcomeMessage -> {
            if (welcomeMessage != null) {
                binding.tvWelcome.setText(welcomeMessage);
            }
        });

        // Observe user info
        mainViewModel.userInfo.observe(this, userInfo -> {
            if (userInfo != null) {
                binding.tvUserInfo.setText(userInfo);
            }
        });
        
        // Observe user role and show appropriate buttons
        mainViewModel.userRole.observe(this, userRole -> {
            if (userRole != null) {
                showRoleSpecificButtons(userRole);
            }
        });
    }
    
    private void showRoleSpecificButtons(String userRole) {
        // Hide both layouts first
        binding.layoutCoachActivities.setVisibility(View.GONE);
        binding.layoutTraineeActivities.setVisibility(View.GONE);
        
        // Show the appropriate layout based on role
        if ("coach".equalsIgnoreCase(userRole)) {
            binding.layoutCoachActivities.setVisibility(View.VISIBLE);
        } else if ("trainee".equalsIgnoreCase(userRole)) {
            binding.layoutTraineeActivities.setVisibility(View.VISIBLE);
        }
        
        // Show the main container
        binding.layoutRoleSpecificButtons.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}