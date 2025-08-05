package com.example.fitness.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.fitness.data.network.model.generated.DetailedWorkoutPlan;
import com.example.fitness.data.network.model.generated.DetailedWorkoutPlanDayExercise;
import com.example.fitness.data.repository.WorkoutsRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class WorkoutDayDetailsViewModel extends ViewModel {
    private final WorkoutsRepository workoutsRepository;

    private final MutableLiveData<List<DetailedWorkoutPlanDayExercise>> _exercises = new MutableLiveData<>();
    public final LiveData<List<DetailedWorkoutPlanDayExercise>> exercises = _exercises;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    @Inject
    public WorkoutDayDetailsViewModel(WorkoutsRepository workoutsRepository) {
        this.workoutsRepository = workoutsRepository;
    }

    public void loadWorkoutPlanAndExtractDay(String planId, int dayNumber) {
        _isLoading.setValue(true);
        workoutsRepository.getWorkoutPlanById(planId, new WorkoutsRepository.WorkoutsCallback<DetailedWorkoutPlan>() {
            @Override
            public void onSuccess(DetailedWorkoutPlan result) {
                _isLoading.setValue(false);
                _error.setValue(null);
                
                // Find the specific day and extract exercises
                if (result.getWorkoutPlanDays() != null) {
                    for (var day : result.getWorkoutPlanDays()) {
                        if (day.getDay() == dayNumber) {
                            _exercises.setValue(day.getExercises());
                            return;
                        }
                    }
                }
                _exercises.setValue(null);
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
