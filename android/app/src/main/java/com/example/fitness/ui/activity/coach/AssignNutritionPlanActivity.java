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
import com.example.fitness.databinding.ActivityAssignNutritionPlanBinding;
import com.example.fitness.ui.adapter.AssignNutritionPlanAdapter;
import com.example.fitness.ui.viewmodel.NutritionPlanViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AssignNutritionPlanActivity extends AppCompatActivity implements AssignNutritionPlanAdapter.OnNutritionPlanAssignListener {
    
    private ActivityAssignNutritionPlanBinding binding;
    private NutritionPlanViewModel viewModel;
    private AssignNutritionPlanAdapter adapter;
    private String traineeId;
    private String traineeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        binding = ActivityAssignNutritionPlanBinding.inflate(getLayoutInflater());
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
        loadNutritionPlans();
    }
    
    private void getIntentData() {
        traineeId = getIntent().getStringExtra("TRAINEE_ID");
        traineeName = getIntent().getStringExtra("TRAINEE_NAME");
    }
    
    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Assign Nutrition Plan");
            getSupportActionBar().setSubtitle("For: " + traineeName);
        }
    }
    
    private void setupRecyclerView() {
        adapter = new AssignNutritionPlanAdapter(this);
        binding.rvNutritionPlans.setLayoutManager(new LinearLayoutManager(this));
        binding.rvNutritionPlans.setAdapter(adapter);
    }
    
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(NutritionPlanViewModel.class);
        
        viewModel.nutritionPlans.observe(this, nutritionPlans -> {
            if (nutritionPlans != null && !nutritionPlans.isEmpty()) {
                adapter.updateNutritionPlans(nutritionPlans);
                binding.tvEmptyState.setVisibility(android.view.View.GONE);
                binding.rvNutritionPlans.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.tvEmptyState.setVisibility(android.view.View.VISIBLE);
                binding.rvNutritionPlans.setVisibility(android.view.View.GONE);
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
                finish();
            }
        });
    }
    
    private void loadNutritionPlans() {
        viewModel.loadNutritionPlans();
    }
    
    @Override
    public void onNutritionPlanSelect(String nutritionPlanId, String nutritionPlanName) {
        viewModel.assignNutritionPlan(nutritionPlanId, traineeId);
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
