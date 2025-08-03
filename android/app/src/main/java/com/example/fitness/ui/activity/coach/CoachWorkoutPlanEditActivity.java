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
import com.example.fitness.data.network.model.generated.BulkUpdateWorkoutPlan;
import com.example.fitness.data.network.model.generated.DetailedWorkoutPlan;
import com.example.fitness.data.repository.ExercisesRepository;
import com.example.fitness.databinding.ActivityCoachWorkoutPlanEditBinding;
import com.example.fitness.ui.adapter.WorkoutDayEditAdapter;
import com.example.fitness.ui.dialog.EditWorkoutExerciseInfoDialogFragment;
import com.example.fitness.ui.dialog.EditWorkoutPlanInfoDialogFragment;
import com.example.fitness.ui.viewmodel.WorkoutPlanEditViewModel;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CoachWorkoutPlanEditActivity extends AppCompatActivity {

    private ActivityCoachWorkoutPlanEditBinding binding;
    private WorkoutPlanEditViewModel viewModel;
    private WorkoutDayEditAdapter dayAdapter;
    private String planId;
    private String planName;
    private boolean isNewPlan = false;

    @Inject
    ExercisesRepository exercisesRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        binding = ActivityCoachWorkoutPlanEditBinding.inflate(getLayoutInflater());
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
            viewModel.loadWorkoutPlan(planId);
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
            getSupportActionBar().setTitle(isNewPlan ? "Create Workout Plan" : "Edit " + (planName != null ? planName : "Workout Plan"));
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(WorkoutPlanEditViewModel.class);
    }

    private void setupRecyclerView() {
        dayAdapter = new WorkoutDayEditAdapter(new WorkoutDayEditAdapter.OnDayActionListener() {
            @Override
            public void onRemoveDay(int dayIndex) {
                viewModel.removeDay(dayIndex);
            }

            @Override
            public void onConvertToRestDay(int dayIndex) {
                viewModel.updateDayRestStatus(dayIndex, true);
            }

            @Override
            public void onConvertToWorkoutDay(int dayIndex) {
                viewModel.updateDayRestStatus(dayIndex, false);
            }

            @Override
            public void onAddExercise(int dayIndex) {
                showAddExerciseDialog(dayIndex);
            }

            @Override
            public void onEditExercise(int dayIndex, int exerciseIndex) {
                showEditExerciseDialog(dayIndex, exerciseIndex);
            }

            @Override
            public void onRemoveExercise(int dayIndex, int exerciseIndex) {
                viewModel.removeExerciseFromDay(dayIndex, exerciseIndex);
            }
        });
        binding.recyclerViewEditDays.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewEditDays.setAdapter(dayAdapter);
        dayAdapter.setExercisesRepository(exercisesRepository);
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        
        binding.buttonAddDay.setOnClickListener(v -> viewModel.addNewDay());
        
        binding.buttonEditPlanInfo.setOnClickListener(v -> showEditPlanInfoDialog());
    }

    private void showEditPlanInfoDialog() {
        String name = viewModel.getCurrentPlanName();
        String description = viewModel.getCurrentPlanDescription();
        BulkUpdateWorkoutPlan.Difficulty difficulty = viewModel.getCurrentPlanDifficulty();
        boolean isActive = viewModel.getCurrentPlanIsActive();

        EditWorkoutPlanInfoDialogFragment dialog = EditWorkoutPlanInfoDialogFragment.newInstance(name, description, difficulty, isActive);
        dialog.setOnWorkoutPlanInfoEditListener((newName, newDescription, newDifficulty, newIsActive) -> {
            viewModel.updatePlanInfo(newName, newDescription, newDifficulty, newIsActive);
            updatePlanInfoDisplay();
        });
        dialog.show(getSupportFragmentManager(), "EditWorkoutPlanInfoDialog");
    }

    private void showAddExerciseDialog(int dayIndex) {
        EditWorkoutExerciseInfoDialogFragment dialog = EditWorkoutExerciseInfoDialogFragment.newInstance(
                0, "", 10, 30, "");
        dialog.setOnWorkoutExerciseInfoEditListener((exerciseTypeId, exerciseTypeName, logType, targetReps, targetDuration, notes) -> {
            viewModel.addExerciseToDay(dayIndex, exerciseTypeId, exerciseTypeName, logType);
            // Update the exercise with the specified values
            if (targetReps != null || targetDuration != null || notes != null) {
                WorkoutPlanEditViewModel.EditablePlanDay day = viewModel.getDayAt(dayIndex);
                if (day != null && !day.exercises.isEmpty()) {
                    int exerciseIndex = day.exercises.size() - 1; // Last added exercise
                    viewModel.updateExercise(dayIndex, exerciseIndex, exerciseTypeId, exerciseTypeName, logType, targetReps, targetDuration, notes);
                }
            }
        });
        dialog.show(getSupportFragmentManager(), "AddExerciseDialog");
    }

    private void showEditExerciseDialog(int dayIndex, int exerciseIndex) {
        WorkoutPlanEditViewModel.EditablePlanExercise exercise = viewModel.getExerciseAt(dayIndex, exerciseIndex);
        if (exercise == null) return;

        EditWorkoutExerciseInfoDialogFragment dialog = EditWorkoutExerciseInfoDialogFragment.newInstance(
                exercise.exerciseTypeId, exercise.exerciseTypeName, exercise.targetReps, 
                exercise.targetDuration, exercise.notes);
        dialog.setOnWorkoutExerciseInfoEditListener((exerciseTypeId, exerciseTypeName, logType, targetReps, targetDuration, notes) -> {
            viewModel.updateExercise(dayIndex, exerciseIndex, exerciseTypeId, exerciseTypeName, logType, targetReps, targetDuration, notes);
        });
        dialog.show(getSupportFragmentManager(), "EditExerciseDialog");
    }

    private void observeViewModel() {
        viewModel.detailedWorkoutPlan.observe(this, this::displayWorkoutPlan);

        viewModel.editableDays.observe(this, editableDays -> {
            if (editableDays != null) {
                dayAdapter.updateDays(editableDays);
            }
        });

        viewModel.isLoading.observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.isSaving.observe(this, isSaving -> {
            // You can show a different progress indicator for saving
            binding.progressBar.setVisibility(isSaving ? View.VISIBLE : View.GONE);
        });

        viewModel.saveSuccess.observe(this, saveSuccess -> {
            if (saveSuccess) {
                Toast.makeText(this, "Workout plan saved successfully", Toast.LENGTH_SHORT).show();
                viewModel.clearSaveSuccess();
                finish(); // Close activity after successful save
            }
        });

        viewModel.error.observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                viewModel.clearError();
            }
        });
    }

    private void displayWorkoutPlan(DetailedWorkoutPlan detailedWorkoutPlan) {
        if (detailedWorkoutPlan == null) {
            updatePlanInfoDisplay();
            return;
        }
        
        binding.textViewPlanName.setText(detailedWorkoutPlan.getName() != null ? detailedWorkoutPlan.getName() : "Unnamed Plan");
        binding.textViewPlanDescription.setText(detailedWorkoutPlan.getDescription() != null ? detailedWorkoutPlan.getDescription() : "No description");
        binding.textViewPlanStatus.setText(detailedWorkoutPlan.isActive() ? "Active" : "Inactive");
    }

    private void updatePlanInfoDisplay() {
        binding.textViewPlanName.setText(viewModel.getCurrentPlanName() != null ? viewModel.getCurrentPlanName() : "Unnamed Plan");
        binding.textViewPlanDescription.setText(viewModel.getCurrentPlanDescription() != null ? viewModel.getCurrentPlanDescription() : "No description");
        binding.textViewPlanStatus.setText(viewModel.getCurrentPlanIsActive() ? "Active" : "Inactive");
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
        
        if (name == null || name.trim().isEmpty()) {
            Toast.makeText(this, "Plan name is required", Toast.LENGTH_SHORT).show();
            showEditPlanInfoDialog();
            return;
        }
        
        if (planId != null) {
            viewModel.savePlan(planId);
        } else {
            // For new plans, you might want to create them first
            Toast.makeText(this, "Creating new plans not yet implemented", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}