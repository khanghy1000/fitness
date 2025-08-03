package com.example.fitness.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.fitness.data.network.model.generated.DetailedWorkoutPlan;
import com.example.fitness.data.repository.WorkoutsRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class WorkoutPlanDetailsViewModel extends ViewModel {
    private final WorkoutsRepository workoutsRepository;

    private final MutableLiveData<DetailedWorkoutPlan> _detailedWorkoutPlan = new MutableLiveData<>();
    public final LiveData<DetailedWorkoutPlan> detailedWorkoutPlan = _detailedWorkoutPlan;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    @Inject
    public WorkoutPlanDetailsViewModel(WorkoutsRepository workoutsRepository) {
        this.workoutsRepository = workoutsRepository;
    }

    public void loadWorkoutPlan(String planId) {
        _isLoading.setValue(true);
        workoutsRepository.getWorkoutPlanById(planId, new WorkoutsRepository.WorkoutsCallback<DetailedWorkoutPlan>() {
            @Override
            public void onSuccess(DetailedWorkoutPlan result) {
                _isLoading.setValue(false);
                _detailedWorkoutPlan.setValue(result);
                _error.setValue(null);
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
}
