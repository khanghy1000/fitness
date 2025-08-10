package com.example.fitness.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.fitness.data.network.model.generated.DetailedWorkoutPlan;
import com.example.fitness.data.network.model.generated.DetailedWorkoutPlanDayExercise;
import com.example.fitness.data.network.model.generated.WorkoutPlanResults;
import com.example.fitness.data.network.model.generated.WorkoutPlanResultsWorkoutPlanDaysInner;
import com.example.fitness.data.network.model.generated.WorkoutPlanResultsWorkoutPlanDaysInnerExercisesInner;
import com.example.fitness.data.repository.AuthRepository;
import com.example.fitness.data.repository.UsersRepository;
import com.example.fitness.data.repository.WorkoutsRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class WorkoutDayDetailsViewModel extends ViewModel {
    private final WorkoutsRepository workoutsRepository;
    private final UsersRepository usersRepository;
    private final AuthRepository authRepository;

    private final MutableLiveData<List<DetailedWorkoutPlanDayExercise>> _exercises = new MutableLiveData<>();
    public final LiveData<List<DetailedWorkoutPlanDayExercise>> exercises = _exercises;

    private final MutableLiveData<List<DetailedWorkoutPlanDayExercise>> _uncompletedExercises = new MutableLiveData<>();
    public final LiveData<List<DetailedWorkoutPlanDayExercise>> uncompletedExercises = _uncompletedExercises;

    private final MutableLiveData<WorkoutPlanResults> _workoutPlanResults = new MutableLiveData<>();
    public final LiveData<WorkoutPlanResults> workoutPlanResults = _workoutPlanResults;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    private final MutableLiveData<String> _currentUserId = new MutableLiveData<>();
    public final LiveData<String> currentUserId = _currentUserId;

    private final MutableLiveData<DetailedWorkoutPlan> _currentPlan = new MutableLiveData<>();
    public final LiveData<DetailedWorkoutPlan> currentPlan = _currentPlan;

    private String userWorkoutPlanId;
    private int currentDayNumber;

    @Inject
    public WorkoutDayDetailsViewModel(WorkoutsRepository workoutsRepository, UsersRepository usersRepository, AuthRepository authRepository) {
        this.workoutsRepository = workoutsRepository;
        this.usersRepository = usersRepository;
        this.authRepository = authRepository;
    }

    public void setUserWorkoutPlanId(String userWorkoutPlanId) {
        this.userWorkoutPlanId = userWorkoutPlanId;
    }

    public void loadCurrentUserId() {
        authRepository.getCurrentUserId()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        userId -> _currentUserId.setValue(userId),
                        error -> _error.setValue("Failed to load user ID: " + error.getMessage())
                );
    }

    public void loadWorkoutPlanAndExtractDay(String planId, int dayNumber) {
        this.currentDayNumber = dayNumber;
        _isLoading.setValue(true);
        workoutsRepository.getWorkoutPlanById(planId, new WorkoutsRepository.WorkoutsCallback<DetailedWorkoutPlan>() {
            @Override
            public void onSuccess(DetailedWorkoutPlan result) {
                _isLoading.setValue(false);
                _error.setValue(null);
                
                // Store the current plan for edit permission checking
                _currentPlan.setValue(result);
                
                // Find the specific day and extract exercises
                if (result.getWorkoutPlanDays() != null) {
                    for (var day : result.getWorkoutPlanDays()) {
                        if (day.getDay() == dayNumber) {
                            _exercises.setValue(day.getExercises());
                            
                            // Load workout plan results to determine uncompleted exercises
                            if (userWorkoutPlanId != null && _currentUserId.getValue() != null) {
                                loadWorkoutPlanResults(day.getExercises());
                            } else {
                                // If no user workout plan, all exercises are uncompleted
                                _uncompletedExercises.setValue(day.getExercises());
                            }
                            return;
                        }
                    }
                }
                _exercises.setValue(null);
                _uncompletedExercises.setValue(null);
            }

            @Override
            public void onError(String error) {
                _isLoading.setValue(false);
                _error.setValue(error);
            }
        });
    }

    private void loadWorkoutPlanResults(List<DetailedWorkoutPlanDayExercise> dayExercises) {
        if (userWorkoutPlanId == null || _currentUserId.getValue() == null) {
            _uncompletedExercises.setValue(dayExercises);
            return;
        }

        usersRepository.getUserWorkoutPlanResults(userWorkoutPlanId, _currentUserId.getValue(), 
            new UsersRepository.UsersCallback<WorkoutPlanResults>() {
                @Override
                public void onSuccess(WorkoutPlanResults result) {
                    _workoutPlanResults.setValue(result);
                    
                    // Determine uncompleted exercises for the current day
                    List<DetailedWorkoutPlanDayExercise> uncompleted = getUncompletedExercisesForDay(result, dayExercises);
                    _uncompletedExercises.setValue(uncompleted);
                }

                @Override
                public void onError(String error) {
                    // If we can't get results, assume all exercises are uncompleted
                    _uncompletedExercises.setValue(dayExercises);
                }
            });
    }

    private List<DetailedWorkoutPlanDayExercise> getUncompletedExercisesForDay(WorkoutPlanResults results, List<DetailedWorkoutPlanDayExercise> dayExercises) {
        if (results == null || results.getWorkoutPlanDays() == null || dayExercises == null) {
            return dayExercises;
        }

        // Find the day in the results
        WorkoutPlanResultsWorkoutPlanDaysInner dayResults = null;
        for (WorkoutPlanResultsWorkoutPlanDaysInner day : results.getWorkoutPlanDays()) {
            if (day.getDay() == currentDayNumber) {
                dayResults = day;
                break;
            }
        }

        if (dayResults == null || dayResults.getExercises() == null) {
            return dayExercises;
        }

        List<DetailedWorkoutPlanDayExercise> uncompleted = new ArrayList<>();
        
        // Check each exercise to see if it's completed
        for (DetailedWorkoutPlanDayExercise exercise : dayExercises) {
            boolean isCompleted = false;
            
            // Look for this exercise in the results
            for (WorkoutPlanResultsWorkoutPlanDaysInnerExercisesInner exerciseResult : dayResults.getExercises()) {
                if (exerciseResult.getId() == (exercise.getId()) &&
                    exerciseResult.getExerciseResults() != null && 
                    !exerciseResult.getExerciseResults().isEmpty()) {
                    isCompleted = true;
                    break;
                }
            }
            
            if (!isCompleted) {
                uncompleted.add(exercise);
            }
        }

        return uncompleted;
    }

    public int getCompletedExerciseCount() {
        List<DetailedWorkoutPlanDayExercise> allExercises = _exercises.getValue();
        List<DetailedWorkoutPlanDayExercise> uncompletedExercises = _uncompletedExercises.getValue();
        
        if (allExercises == null) return 0;
        if (uncompletedExercises == null) return allExercises.size();
        
        return allExercises.size() - uncompletedExercises.size();
    }

    public int getTotalExerciseCount() {
        List<DetailedWorkoutPlanDayExercise> allExercises = _exercises.getValue();
        return allExercises != null ? allExercises.size() : 0;
    }

    public boolean isWorkoutCompleted() {
        List<DetailedWorkoutPlanDayExercise> uncompletedExercises = _uncompletedExercises.getValue();
        return uncompletedExercises != null && uncompletedExercises.isEmpty();
    }

    public void clearError() {
        _error.setValue(null);
    }
}
