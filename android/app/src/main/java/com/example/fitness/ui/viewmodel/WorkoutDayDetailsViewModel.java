package com.example.fitness.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.fitness.data.network.model.generated.DetailedWorkoutPlan;
import com.example.fitness.data.network.model.generated.DetailedWorkoutPlanDayExercise;
import com.example.fitness.data.repository.AuthRepository;
import com.example.fitness.data.repository.WorkoutsRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class WorkoutDayDetailsViewModel extends ViewModel {
    private final WorkoutsRepository workoutsRepository;
    private final AuthRepository authRepository;

    private final MutableLiveData<List<DetailedWorkoutPlanDayExercise>> _exercises = new MutableLiveData<>();
    public final LiveData<List<DetailedWorkoutPlanDayExercise>> exercises = _exercises;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    private final MutableLiveData<String> _currentUserId = new MutableLiveData<>();
    public final LiveData<String> currentUserId = _currentUserId;

    private final MutableLiveData<DetailedWorkoutPlan> _currentPlan = new MutableLiveData<>();
    public final LiveData<DetailedWorkoutPlan> currentPlan = _currentPlan;

    @Inject
    public WorkoutDayDetailsViewModel(WorkoutsRepository workoutsRepository, AuthRepository authRepository) {
        this.workoutsRepository = workoutsRepository;
        this.authRepository = authRepository;
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
