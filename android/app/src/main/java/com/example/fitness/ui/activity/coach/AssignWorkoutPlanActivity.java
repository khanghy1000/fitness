package com.example.fitness.ui.activity.coach;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.fitness.R;
import com.example.fitness.databinding.ActivityAssignWorkoutPlanBinding;
import com.example.fitness.ui.adapter.AssignWorkoutPlanAdapter;
import com.example.fitness.ui.viewmodel.WorkoutPlanViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AssignWorkoutPlanActivity extends AppCompatActivity implements AssignWorkoutPlanAdapter.OnWorkoutPlanAssignListener {
    
    private ActivityAssignWorkoutPlanBinding binding;
    private WorkoutPlanViewModel viewModel;
    private AssignWorkoutPlanAdapter adapter;
    private String traineeId;
    private String traineeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        binding = ActivityAssignWorkoutPlanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        getIntentData();
        setupToolbar();
        setupRecyclerView();
        setupViewModel();
        loadWorkoutPlans();
    }
    
    private void getIntentData() {
        traineeId = getIntent().getStringExtra("TRAINEE_ID");
        traineeName = getIntent().getStringExtra("TRAINEE_NAME");
    }
    
    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Assign Workout Plan");
            getSupportActionBar().setSubtitle("For: " + traineeName);
        }
    }
    
    private void setupRecyclerView() {
        adapter = new AssignWorkoutPlanAdapter(this);
        binding.rvWorkoutPlans.setLayoutManager(new LinearLayoutManager(this));
        binding.rvWorkoutPlans.setAdapter(adapter);
    }
    
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(WorkoutPlanViewModel.class);
        
        viewModel.workoutPlans.observe(this, workoutPlans -> {
            if (workoutPlans != null && !workoutPlans.isEmpty()) {
                adapter.updateWorkoutPlans(workoutPlans);
                binding.tvEmptyState.setVisibility(android.view.View.GONE);
                binding.rvWorkoutPlans.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.tvEmptyState.setVisibility(android.view.View.VISIBLE);
                binding.rvWorkoutPlans.setVisibility(android.view.View.GONE);
            }
        });
        
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
                setResult(RESULT_OK);
                finish();
            }
        });
    }
    
    private void loadWorkoutPlans() {
        viewModel.loadWorkoutPlans();
    }
    
    @Override
    public void onWorkoutPlanSelect(String workoutPlanId, String workoutPlanName) {
        viewModel.assignWorkoutPlan(workoutPlanId, traineeId);
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
