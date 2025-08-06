package com.example.fitness.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.fitness.data.network.model.generated.CreateWorkoutPlan;
import com.example.fitness.data.network.model.generated.WorkoutPlan;
import com.example.fitness.data.network.model.generated.WorkoutPlanAssignment;
import com.example.fitness.data.repository.UsersRepository;
import com.example.fitness.data.repository.WorkoutsRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class WorkoutPlanViewModel extends ViewModel {
    private final UsersRepository usersRepository;
    private final WorkoutsRepository workoutsRepository;

    private final MutableLiveData<List<WorkoutPlan>> _workoutPlans = new MutableLiveData<>();
    public final LiveData<List<WorkoutPlan>> workoutPlans = _workoutPlans;

    private final MutableLiveData<List<WorkoutPlanAssignment>> _userWorkoutPlanAssignments = new MutableLiveData<>();
    public final LiveData<List<WorkoutPlanAssignment>> userWorkoutPlanAssignments = _userWorkoutPlanAssignments;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    private final MutableLiveData<Boolean> _isRefreshing = new MutableLiveData<>();
    public final LiveData<Boolean> isRefreshing = _isRefreshing;

    private final MutableLiveData<WorkoutPlan> _createdPlan = new MutableLiveData<>();
    public final LiveData<WorkoutPlan> createdPlan = _createdPlan;

    @Inject
    public WorkoutPlanViewModel(UsersRepository usersRepository, WorkoutsRepository workoutsRepository) {
        this.usersRepository = usersRepository;
        this.workoutsRepository = workoutsRepository;
        loadWorkoutPlans();
    }

    public void loadWorkoutPlans() {
        _isLoading.setValue(true);
        workoutsRepository.getAllWorkoutPlans(new WorkoutsRepository.WorkoutsCallback<List<WorkoutPlan>>() {
            @Override
            public void onSuccess(List<WorkoutPlan> result) {
                _isLoading.setValue(false);
                _isRefreshing.setValue(false);
                _workoutPlans.setValue(result);
                _error.setValue(null);
            }

            @Override
            public void onError(String error) {
                _isLoading.setValue(false);
                _isRefreshing.setValue(false);
                _error.setValue(error);
            }
        });
    }

    // Load trainee workout plan assignments
    public void loadUserWorkoutPlanAssignments() {
        _isLoading.setValue(true);
        usersRepository.getUserWorkoutPlans(new UsersRepository.UsersCallback<List<WorkoutPlanAssignment>>() {
            @Override
            public void onSuccess(List<WorkoutPlanAssignment> result) {
                _isLoading.setValue(false);
                _isRefreshing.setValue(false);
                
                // Filter only active workout plan assignments
                List<WorkoutPlanAssignment> activeAssignments = new ArrayList<>();
                for (WorkoutPlanAssignment assignment : result) {
                    if (assignment.getStatus() == WorkoutPlanAssignment.Status.active) {
                        activeAssignments.add(assignment);
                    }
                }
                
                _userWorkoutPlanAssignments.setValue(activeAssignments);
                _error.setValue(null);
            }

            @Override
            public void onError(String error) {
                _isLoading.setValue(false);
                _isRefreshing.setValue(false);
                _error.setValue(error);
            }
        });
    }

    public void refreshWorkoutPlans() {
        _isRefreshing.setValue(true);
        loadWorkoutPlans();
    }

    // Refresh trainee workout plan assignments
    public void refreshUserWorkoutPlanAssignments() {
        _isRefreshing.setValue(true);
        loadUserWorkoutPlanAssignments();
    }

    public void createWorkoutPlan(String name, String description, CreateWorkoutPlan.Difficulty difficulty) {
        _isLoading.setValue(true);
        
        CreateWorkoutPlan createWorkoutPlan = new CreateWorkoutPlan(name, description, difficulty, null);
        
        workoutsRepository.createWorkoutPlan(createWorkoutPlan, new WorkoutsRepository.WorkoutsCallback<WorkoutPlan>() {
            @Override
            public void onSuccess(WorkoutPlan result) {
                _isLoading.setValue(false);
                _createdPlan.setValue(result);
                _error.setValue(null);
                // Refresh the list
                loadWorkoutPlans();
            }

            @Override
            public void onError(String error) {
                _isLoading.setValue(false);
                _error.setValue(error);
            }
        });
    }

    public void clearError() {
        _error.setValue(null);
    }

    public void clearCreatedPlan() {
        _createdPlan.setValue(null);
    }
}
