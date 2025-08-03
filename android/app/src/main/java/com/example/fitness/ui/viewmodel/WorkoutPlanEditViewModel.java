package com.example.fitness.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.fitness.data.network.model.generated.BulkUpdateWorkoutPlan;
import com.example.fitness.data.network.model.generated.BulkWorkoutPlanDay;
import com.example.fitness.data.network.model.generated.BulkWorkoutPlanDayExercise;
import com.example.fitness.data.network.model.generated.DetailedWorkoutPlan;
import com.example.fitness.data.network.model.generated.ExerciseType;
import com.example.fitness.data.repository.ExercisesRepository;
import com.example.fitness.data.repository.WorkoutsRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import lombok.Data;

@HiltViewModel
public class WorkoutPlanEditViewModel extends ViewModel {
    private final WorkoutsRepository workoutsRepository;
    private final ExercisesRepository exercisesRepository;

    private final MutableLiveData<DetailedWorkoutPlan> _detailedWorkoutPlan = new MutableLiveData<>();
    public final LiveData<DetailedWorkoutPlan> detailedWorkoutPlan = _detailedWorkoutPlan;

    private final MutableLiveData<List<EditablePlanDay>> _editableDays = new MutableLiveData<>();
    public final LiveData<List<EditablePlanDay>> editableDays = _editableDays;

    private final MutableLiveData<List<ExerciseType>> _exerciseTypes = new MutableLiveData<>();
    public final LiveData<List<ExerciseType>> exerciseTypes = _exerciseTypes;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<Boolean> _isSaving = new MutableLiveData<>();
    public final LiveData<Boolean> isSaving = _isSaving;

    private final MutableLiveData<Boolean> _saveSuccess = new MutableLiveData<>();
    public final LiveData<Boolean> saveSuccess = _saveSuccess;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    // Current plan state
    private String currentPlanName;
    private String currentPlanDescription;
    private BulkUpdateWorkoutPlan.Difficulty currentPlanDifficulty;
    private boolean currentPlanIsActive;

    @Data
    public static class EditablePlanDay {
        public Integer id;
        public int day;
        public boolean isRestDay;
        public List<EditablePlanExercise> exercises = new ArrayList<>();
    }

    @Data
    public static class EditablePlanExercise {
        public Integer id;
        public int exerciseTypeId;
        public String exerciseTypeName;
        public ExerciseType.LogType logType;
        public Integer order;
        public Integer targetReps;
        public Integer targetDuration;
        public String notes;
    }

    @Inject
    public WorkoutPlanEditViewModel(WorkoutsRepository workoutsRepository, ExercisesRepository exercisesRepository) {
        this.workoutsRepository = workoutsRepository;
        this.exercisesRepository = exercisesRepository;
        loadExerciseTypes();
    }

    public void loadWorkoutPlan(String planId) {
        _isLoading.setValue(true);
        workoutsRepository.getWorkoutPlanById(planId, new WorkoutsRepository.WorkoutsCallback<DetailedWorkoutPlan>() {
            @Override
            public void onSuccess(DetailedWorkoutPlan result) {
                _isLoading.setValue(false);
                _detailedWorkoutPlan.setValue(result);
                _error.setValue(null);
                convertToEditableFormat(result);
            }

            @Override
            public void onError(String error) {
                _isLoading.setValue(false);
                _error.setValue(error);
            }
        });
    }

    public void initializeForNewPlan() {
        currentPlanName = "New Workout Plan";
        currentPlanDescription = "";
        currentPlanDifficulty = BulkUpdateWorkoutPlan.Difficulty.beginner;
        currentPlanIsActive = true;
        
        List<EditablePlanDay> days = new ArrayList<>();
        _editableDays.setValue(days);
    }

    private void loadExerciseTypes() {
        exercisesRepository.getAllExerciseTypes(new ExercisesRepository.ExercisesCallback<List<ExerciseType>>() {
            @Override
            public void onSuccess(List<ExerciseType> result) {
                _exerciseTypes.setValue(result);
            }

            @Override
            public void onError(String error) {
                // Handle silently for now
            }
        });
    }

    private void convertToEditableFormat(DetailedWorkoutPlan plan) {
        currentPlanName = plan.getName();
        currentPlanDescription = plan.getDescription();
        currentPlanDifficulty = plan.getDifficulty() != null ? 
            BulkUpdateWorkoutPlan.Difficulty.valueOf(plan.getDifficulty().getValue()) : 
            BulkUpdateWorkoutPlan.Difficulty.beginner;
        currentPlanIsActive = plan.isActive();

        List<EditablePlanDay> editableDays = new ArrayList<>();
        if (plan.getWorkoutPlanDays() != null) {
            for (var day : plan.getWorkoutPlanDays()) {
                EditablePlanDay editableDay = new EditablePlanDay();
                editableDay.id = day.getId();
                editableDay.day = day.getDay();
                editableDay.isRestDay = day.isRestDay();

                if (day.getExercises() != null) {
                    for (var exercise : day.getExercises()) {
                        EditablePlanExercise editableExercise = new EditablePlanExercise();
                        editableExercise.id = exercise.getId();
                        editableExercise.exerciseTypeId = exercise.getExerciseTypeId();
                        editableExercise.exerciseTypeName = exercise.getExerciseType().getName();
                        editableExercise.logType = exercise.getExerciseType().getLogType();
                        editableExercise.order = exercise.getOrder();
                        editableExercise.targetReps = exercise.getTargetReps();
                        editableExercise.targetDuration = exercise.getTargetDuration();
                        editableExercise.notes = exercise.getNotes();
                        editableDay.exercises.add(editableExercise);
                    }
                }
                editableDays.add(editableDay);
            }
        }
        _editableDays.setValue(editableDays);
    }

    public void addNewDay() {
        List<EditablePlanDay> currentDays = _editableDays.getValue();
        if (currentDays == null) currentDays = new ArrayList<>();

        EditablePlanDay newDay = new EditablePlanDay();
        newDay.day = currentDays.size() + 1;
        newDay.isRestDay = false;

        currentDays.add(newDay);
        _editableDays.setValue(currentDays);
    }

    public void removeDay(int dayIndex) {
        List<EditablePlanDay> currentDays = _editableDays.getValue();
        if (currentDays != null && dayIndex < currentDays.size()) {
            currentDays.remove(dayIndex);
            // Update day numbers
            for (int i = 0; i < currentDays.size(); i++) {
                currentDays.get(i).day = i + 1;
            }
            _editableDays.setValue(currentDays);
        }
    }

    public void updateDayRestStatus(int dayIndex, boolean isRestDay) {
        List<EditablePlanDay> currentDays = _editableDays.getValue();
        if (currentDays != null && dayIndex < currentDays.size()) {
            currentDays.get(dayIndex).isRestDay = isRestDay;
            if (isRestDay) {
                currentDays.get(dayIndex).exercises.clear();
            }
            _editableDays.setValue(currentDays);
        }
    }

    public void addExerciseToDay(int dayIndex, int exerciseTypeId, String exerciseTypeName, ExerciseType.LogType logType) {
        List<EditablePlanDay> currentDays = _editableDays.getValue();
        if (currentDays != null && dayIndex < currentDays.size()) {
            EditablePlanExercise newExercise = new EditablePlanExercise();
            newExercise.exerciseTypeId = exerciseTypeId;
            newExercise.exerciseTypeName = exerciseTypeName;
            newExercise.logType = logType;
            newExercise.order = currentDays.get(dayIndex).exercises.size() + 1;
            newExercise.targetReps = 10;
            newExercise.targetDuration = 30;

            currentDays.get(dayIndex).exercises.add(newExercise);
            _editableDays.setValue(currentDays);
        }
    }

    public void removeExerciseFromDay(int dayIndex, int exerciseIndex) {
        List<EditablePlanDay> currentDays = _editableDays.getValue();
        if (currentDays != null && dayIndex < currentDays.size()) {
            List<EditablePlanExercise> exercises = currentDays.get(dayIndex).exercises;
            if (exerciseIndex < exercises.size()) {
                exercises.remove(exerciseIndex);
                // Update order
                for (int i = 0; i < exercises.size(); i++) {
                    exercises.get(i).order = i + 1;
                }
                _editableDays.setValue(currentDays);
            }
        }
    }

    public void updateExercise(int dayIndex, int exerciseIndex, int exerciseTypeId, String exerciseTypeName, ExerciseType.LogType logType, Integer targetReps, Integer targetDuration, String notes) {
        List<EditablePlanDay> currentDays = _editableDays.getValue();
        if (currentDays != null && dayIndex < currentDays.size()) {
            List<EditablePlanExercise> exercises = currentDays.get(dayIndex).exercises;
            if (exerciseIndex < exercises.size()) {
                EditablePlanExercise exercise = exercises.get(exerciseIndex);
                exercise.exerciseTypeId = exerciseTypeId;
                exercise.exerciseTypeName = exerciseTypeName;
                exercise.logType = logType;
                exercise.targetReps = targetReps;
                exercise.targetDuration = targetDuration;
                exercise.notes = notes;
                _editableDays.setValue(currentDays);
            }
        }
    }

    public void updatePlanInfo(String name, String description, BulkUpdateWorkoutPlan.Difficulty difficulty, boolean isActive) {
        currentPlanName = name;
        currentPlanDescription = description;
        currentPlanDifficulty = difficulty;
        currentPlanIsActive = isActive;
    }

    public void savePlan(String planId) {
        _isSaving.setValue(true);

        List<BulkWorkoutPlanDay> bulkDays = new ArrayList<>();
        List<EditablePlanDay> currentDays = _editableDays.getValue();
        if (currentDays != null) {
            for (EditablePlanDay day : currentDays) {
                List<BulkWorkoutPlanDayExercise> bulkExercises = new ArrayList<>();
                if (!day.isRestDay) {
                    for (EditablePlanExercise exercise : day.exercises) {
                        BulkWorkoutPlanDayExercise bulkExercise = new BulkWorkoutPlanDayExercise(
                            exercise.exerciseTypeId,
                            exercise.id,
                            exercise.order,
                            exercise.targetReps,
                            exercise.targetDuration,
                            exercise.notes
                        );
                        bulkExercises.add(bulkExercise);
                    }
                }

                BulkWorkoutPlanDay bulkDay = new BulkWorkoutPlanDay(
                    day.day,
                    day.id,
                    day.isRestDay,
                    bulkExercises.isEmpty() ? null : bulkExercises
                );
                bulkDays.add(bulkDay);
            }
        }

        BulkUpdateWorkoutPlan bulkUpdate = new BulkUpdateWorkoutPlan(
            currentPlanName,
            currentPlanDescription,
            currentPlanDifficulty,
            currentPlanIsActive,
            bulkDays
        );

        workoutsRepository.bulkUpdateWorkoutPlan(planId, bulkUpdate, new WorkoutsRepository.WorkoutsCallback<DetailedWorkoutPlan>() {
            @Override
            public void onSuccess(DetailedWorkoutPlan result) {
                _isSaving.setValue(false);
                _saveSuccess.setValue(true);
                _detailedWorkoutPlan.setValue(result);
                convertToEditableFormat(result);
            }

            @Override
            public void onError(String error) {
                _isSaving.setValue(false);
                _error.setValue(error);
            }
        });
    }

    // Getters for current plan state
    public String getCurrentPlanName() {
        return currentPlanName;
    }

    public String getCurrentPlanDescription() {
        return currentPlanDescription;
    }

    public BulkUpdateWorkoutPlan.Difficulty getCurrentPlanDifficulty() {
        return currentPlanDifficulty;
    }

    public boolean getCurrentPlanIsActive() {
        return currentPlanIsActive;
    }

    public EditablePlanDay getDayAt(int index) {
        List<EditablePlanDay> currentDays = _editableDays.getValue();
        if (currentDays != null && index < currentDays.size()) {
            return currentDays.get(index);
        }
        return null;
    }

    public EditablePlanExercise getExerciseAt(int dayIndex, int exerciseIndex) {
        EditablePlanDay day = getDayAt(dayIndex);
        if (day != null && exerciseIndex < day.exercises.size()) {
            return day.exercises.get(exerciseIndex);
        }
        return null;
    }

    public void clearError() {
        _error.setValue(null);
    }

    public void clearSaveSuccess() {
        _saveSuccess.setValue(false);
    }
}
