package com.example.fitness.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.fitness.databinding.FragmentHomeBinding;
import com.example.fitness.ui.activity.MessageActivity;
import com.example.fitness.ui.activity.coach.CoachNutritionPlanActivity;
import com.example.fitness.ui.activity.coach.CoachTraineesActivity;
import com.example.fitness.ui.activity.coach.CoachWorkoutPlanActivity;
import com.example.fitness.ui.activity.trainee.TraineeCoachActivity;
import com.example.fitness.ui.activity.trainee.TraineeWorkoutPlanActivity;
import com.example.fitness.ui.activity.trainee.TraineeNutritionPlanActivity;
import com.example.fitness.ui.activity.trainee.TraineeStatsActivity;
import com.example.fitness.ui.viewmodel.MainViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment {
    
    private FragmentHomeBinding binding;
    private MainViewModel mainViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Get the shared ViewModel from the parent activity
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        
        setupClickListeners();
        observeViewModel();
    }

    private void setupClickListeners() {
        setupCoachActivityClickListeners();
        setupTraineeActivityClickListeners();
    }
    
    private void setupCoachActivityClickListeners() {
        binding.btnCoachTrainee.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CoachTraineesActivity.class);
            startActivity(intent);
        });
        
        binding.btnCoachExercisePlan.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CoachWorkoutPlanActivity.class);
            startActivity(intent);
        });
        
        binding.btnCoachNutritionPlan.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CoachNutritionPlanActivity.class);
            startActivity(intent);
        });

        binding.btnCoachMessage.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MessageActivity.class);
            startActivity(intent);
        });
    }
    
    private void setupTraineeActivityClickListeners() {
        binding.btnTraineeCoach.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), TraineeCoachActivity.class);
            startActivity(intent);
        });
        
        binding.btnTraineeExercisePlan.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), TraineeWorkoutPlanActivity.class);
            startActivity(intent);
        });
        
        binding.btnTraineeNutritionPlan.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), TraineeNutritionPlanActivity.class);
            startActivity(intent);
        });
        
        binding.btnTraineeStats.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), TraineeStatsActivity.class);
            startActivity(intent);
        });

        binding.btnTraineeMessage.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MessageActivity.class);
            startActivity(intent);
        });
    }

    private void observeViewModel() {
        // Observe welcome message
        mainViewModel.welcomeMessage.observe(getViewLifecycleOwner(), welcomeMessage -> {
            if (welcomeMessage != null) {
                binding.tvWelcome.setText(welcomeMessage);
            }
        });
        
        // Observe user role and show appropriate buttons
        mainViewModel.userRole.observe(getViewLifecycleOwner(), userRole -> {
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
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
