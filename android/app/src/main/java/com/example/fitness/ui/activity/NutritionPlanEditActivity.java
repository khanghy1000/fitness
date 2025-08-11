package com.example.fitness.ui.activity;

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
import com.example.fitness.data.network.model.generated.DetailedNutritionPlan;
import com.example.fitness.databinding.ActivityNutritionPlanEditBinding;
import com.example.fitness.ui.adapter.NutritionDayEditAdapter;
import com.example.fitness.ui.dialog.EditDayInfoDialogFragment;
import com.example.fitness.ui.dialog.EditFoodInfoDialogFragment;
import com.example.fitness.ui.dialog.EditMealInfoDialogFragment;
import com.example.fitness.ui.dialog.EditPlanInfoDialogFragment;
import com.example.fitness.ui.viewmodel.NutritionPlanEditViewModel;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NutritionPlanEditActivity extends AppCompatActivity {

    private ActivityNutritionPlanEditBinding binding;
    private NutritionPlanEditViewModel viewModel;
    private NutritionDayEditAdapter dayAdapter;
    private String planId;
    private String planName;
    private boolean isNewPlan = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        binding = ActivityNutritionPlanEditBinding.inflate(getLayoutInflater());
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
            public void onEditDay(int dayIndex) {
                showEditDayDialog(dayIndex);
            }

            @Override
            public void onAddMeal(int dayIndex) {
                viewModel.addMealToDay(dayIndex);
            }

            @Override
            public void onEditMeal(int dayIndex, int mealIndex) {
                showEditMealDialog(dayIndex, mealIndex);
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
            public void onEditFood(int dayIndex, int mealIndex, int foodIndex) {
                showEditFoodDialog(dayIndex, mealIndex, foodIndex);
            }

            @Override
            public void onRemoveFood(int dayIndex, int mealIndex, int foodIndex) {
                viewModel.removeFoodFromMeal(dayIndex, mealIndex, foodIndex);
            }

            @Override
            public void onRemoveDay(int dayIndex) {
                viewModel.removeDay(dayIndex);
            }
        }, viewModel);
        binding.recyclerViewEditDays.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewEditDays.setAdapter(dayAdapter);
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        
        binding.buttonAddDay.setOnClickListener(v -> viewModel.addNewDay());
        
        binding.buttonEditPlanInfo.setOnClickListener(v -> showEditPlanInfoDialog());
    }

    private void showEditPlanInfoDialog() {
        String name = viewModel.getCurrentPlanName();
        String description = viewModel.getCurrentPlanDescription();
        boolean isActive = viewModel.getCurrentPlanIsActive();

        EditPlanInfoDialogFragment dialog = EditPlanInfoDialogFragment.newInstance(name, description, isActive);
        dialog.setOnPlanInfoEditListener((newName, newDescription, newIsActive) -> {
            viewModel.updatePlanInfo(newName, newDescription, newIsActive);
        });
        dialog.show(getSupportFragmentManager(), "EditPlanInfoDialog");
    }

    private void showEditDayDialog(int dayIndex) {
        NutritionPlanEditViewModel.EditablePlanDay day = viewModel.getDayAt(dayIndex);
        if (day == null) return;

        ArrayList<String> availableWeekdays = new ArrayList<>(viewModel.getAvailableWeekdays());
        
        EditDayInfoDialogFragment dialog = EditDayInfoDialogFragment.newInstance(
                day.weekday, availableWeekdays);
        dialog.setOnDayInfoEditListener((weekday) -> {
            viewModel.updateDayWeekday(dayIndex, weekday);
        });
        dialog.show(getSupportFragmentManager(), "EditDayInfoDialog");
    }

    private void showEditMealDialog(int dayIndex, int mealIndex) {
        NutritionPlanEditViewModel.EditablePlanMeal meal = viewModel.getMealAt(dayIndex, mealIndex);
        if (meal == null) return;

        EditMealInfoDialogFragment dialog = EditMealInfoDialogFragment.newInstance(
                meal.name, meal.time);
        dialog.setOnMealInfoEditListener((name, time) -> {
            viewModel.updateMealName(dayIndex, mealIndex, name);
            viewModel.updateMealTime(dayIndex, mealIndex, time);
        });
        dialog.show(getSupportFragmentManager(), "EditMealInfoDialog");
    }

    private void showEditFoodDialog(int dayIndex, int mealIndex, int foodIndex) {
        NutritionPlanEditViewModel.EditablePlanFood food = viewModel.getFoodAt(dayIndex, mealIndex, foodIndex);
        if (food == null) return;

        EditFoodInfoDialogFragment dialog = EditFoodInfoDialogFragment.newInstance(
                food.name, food.quantity, food.calories, food.protein, food.carbs, food.fat, food.fiber);
        dialog.setOnFoodInfoEditListener((name, quantity, calories, protein, carbs, fat, fiber) -> {
            viewModel.updateFoodName(dayIndex, mealIndex, foodIndex, name);
            viewModel.updateFoodQuantity(dayIndex, mealIndex, foodIndex, quantity);
            viewModel.updateFoodCalories(dayIndex, mealIndex, foodIndex, calories);
            viewModel.updateFoodProtein(dayIndex, mealIndex, foodIndex, protein);
            viewModel.updateFoodCarbs(dayIndex, mealIndex, foodIndex, carbs);
            viewModel.updateFoodFat(dayIndex, mealIndex, foodIndex, fat);
            viewModel.updateFoodFiber(dayIndex, mealIndex, foodIndex, fiber);
        });
        dialog.show(getSupportFragmentManager(), "EditFoodInfoDialog");
    }

    private void observeViewModel() {
        viewModel.detailedNutritionPlan.observe(this, this::displayNutritionPlan);

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

    private void displayNutritionPlan(DetailedNutritionPlan detailedNutritionPlan) {
        if (detailedNutritionPlan == null) {
            // For new plans, show the stored values
            binding.textViewPlanName.setText(viewModel.getCurrentPlanName().isEmpty() ? "New Plan" : viewModel.getCurrentPlanName());
            binding.textViewPlanDescription.setText(viewModel.getCurrentPlanDescription().isEmpty() ? "No description" : viewModel.getCurrentPlanDescription());
            binding.textViewPlanStatus.setText(viewModel.getCurrentPlanIsActive() ? "Active" : "Inactive");
            return;
        }
        
        binding.textViewPlanName.setText(detailedNutritionPlan.getName() != null ? detailedNutritionPlan.getName() : "Unnamed Plan");
        binding.textViewPlanDescription.setText(detailedNutritionPlan.getDescription() != null ? detailedNutritionPlan.getDescription() : "No description");
        binding.textViewPlanStatus.setText(detailedNutritionPlan.isActive() ? "Active" : "Inactive");
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
        String name = viewModel.getCurrentPlanName();
        String description = viewModel.getCurrentPlanDescription();
        boolean isActive = viewModel.getCurrentPlanIsActive();
        
        if (name == null || name.trim().isEmpty()) {
            Toast.makeText(this, "Please set a plan name before saving", Toast.LENGTH_SHORT).show();
            showEditPlanInfoDialog();
            return;
        }
        
        viewModel.savePlan(name, description, isActive);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}