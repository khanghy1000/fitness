package com.example.fitness.ui.activity.coach;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.fitness.R;
import com.example.fitness.data.network.model.generated.NutritionPlan;
import com.example.fitness.databinding.ActivityCoachNutritionPlanEditBinding;
import com.example.fitness.ui.adapter.NutritionDayEditAdapter;
import com.example.fitness.ui.viewmodel.NutritionPlanEditViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CoachNutritionPlanEditActivity extends AppCompatActivity {

    private ActivityCoachNutritionPlanEditBinding binding;
    private NutritionPlanEditViewModel viewModel;
    private NutritionDayEditAdapter dayAdapter;
    private String planId;
    private String planName;
    private boolean isNewPlan = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        binding = ActivityCoachNutritionPlanEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getIntentData();
        initializeViews();
        setupViewModel();
        setupRecyclerView();
        setupListeners();
        observeViewModel();
        
        if (isNewPlan) {
            viewModel.initializeForNewPlan();
        } else if (planId != null) {
            viewModel.initializeForExistingPlan(planId);
        }
    }

    private void getIntentData() {
        Intent intent = getIntent();
        int planIdInt = intent.getIntExtra("PLAN_ID", -1);
        planName = intent.getStringExtra("PLAN_NAME");
        
        if (planIdInt == -1) {
            isNewPlan = true;
            planId = null;
        } else {
            isNewPlan = false;
            planId = String.valueOf(planIdInt);
        }
    }

    private void initializeViews() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(isNewPlan ? "Create Nutrition Plan" : "Edit " + (planName != null ? planName : "Nutrition Plan"));
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(NutritionPlanEditViewModel.class);
    }

    private void setupRecyclerView() {
        dayAdapter = new NutritionDayEditAdapter(new NutritionDayEditAdapter.OnDayActionListener() {
            @Override
            public void onAddMeal(int dayIndex) {
                viewModel.addMealToDay(dayIndex);
            }

            @Override
            public void onRemoveMeal(int dayIndex, int mealIndex) {
                viewModel.removeMealFromDay(dayIndex, mealIndex);
            }

            @Override
            public void onAddFood(int dayIndex, int mealIndex) {
                viewModel.addFoodToMeal(dayIndex, mealIndex);
            }

            @Override
            public void onRemoveFood(int dayIndex, int mealIndex, int foodIndex) {
                viewModel.removeFoodFromMeal(dayIndex, mealIndex, foodIndex);
            }

            @Override
            public void onRemoveDay(int dayIndex) {
                viewModel.removeDay(dayIndex);
            }
        });
        binding.recyclerViewEditDays.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewEditDays.setAdapter(dayAdapter);
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        
        binding.buttonAddDay.setOnClickListener(v -> viewModel.addNewDay());
    }

    private void observeViewModel() {
        viewModel.nutritionPlan.observe(this, this::displayNutritionPlan);

        viewModel.editableDays.observe(this, editableDays -> {
            if (editableDays != null) {
                dayAdapter.updateDays(editableDays);
            }
        });

        viewModel.isLoading.observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.isSaving.observe(this, isSaving -> {
            binding.progressBar.setVisibility(isSaving ? View.VISIBLE : View.GONE);
        });

        viewModel.saveSuccess.observe(this, saveSuccess -> {
            if (saveSuccess) {
                Toast.makeText(this, "Nutrition plan saved successfully", Toast.LENGTH_SHORT).show();
                viewModel.clearSaveSuccess();
                finish();
            }
        });

        viewModel.error.observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                viewModel.clearError();
            }
        });
    }

    private void displayNutritionPlan(NutritionPlan nutritionPlan) {
        if (nutritionPlan == null) return;
        
        binding.editTextPlanName.setText(nutritionPlan.getName());
        binding.editTextPlanDescription.setText(nutritionPlan.getDescription());
        binding.switchIsActive.setChecked(nutritionPlan.isActive());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            savePlan();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void savePlan() {
        String name = binding.editTextPlanName.getText() != null ? binding.editTextPlanName.getText().toString().trim() : "";
        String description = binding.editTextPlanDescription.getText() != null ? binding.editTextPlanDescription.getText().toString().trim() : "";
        boolean isActive = binding.switchIsActive.isChecked();
        
        if (name.isEmpty()) {
            binding.textInputLayoutPlanName.setError("Plan name is required");
            return;
        }
        
        binding.textInputLayoutPlanName.setError(null);
        
        viewModel.savePlan(name, description.isEmpty() ? null : description, isActive);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}