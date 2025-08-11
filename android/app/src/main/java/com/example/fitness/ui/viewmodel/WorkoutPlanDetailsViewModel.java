package com.example.fitness.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.fitness.data.network.model.generated.DetailedWorkoutPlan;
import com.example.fitness.data.network.model.generated.WorkoutPlanResults;
import com.example.fitness.data.repository.AuthRepository;
import com.example.fitness.data.repository.UsersRepository;
import com.example.fitness.data.repository.WorkoutsRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class WorkoutPlanDetailsViewModel extends ViewModel {
    private final WorkoutsRepository workoutsRepository;
    private final UsersRepository usersRepository;
    private final AuthRepository authRepository;

    private final MutableLiveData<DetailedWorkoutPlan> _detailedWorkoutPlan = new MutableLiveData<>();
    public final LiveData<DetailedWorkoutPlan> detailedWorkoutPlan = _detailedWorkoutPlan;

    private final MutableLiveData<WorkoutPlanResults> _workoutPlanResults = new MutableLiveData<>();
    public final LiveData<WorkoutPlanResults> workoutPlanResults = _workoutPlanResults;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    private final MutableLiveData<String> _currentUserId = new MutableLiveData<>();
    public final LiveData<String> currentUserId = _currentUserId;

    private String userWorkoutPlanId;

    @Inject
    public WorkoutPlanDetailsViewModel(WorkoutsRepository workoutsRepository, UsersRepository usersRepository, AuthRepository authRepository) {
        this.workoutsRepository = workoutsRepository;
        this.usersRepository = usersRepository;
        this.authRepository = authRepository;
        loadCurrentUserId();
    }

    public void setUserWorkoutPlanId(String userWorkoutPlanId) {
        this.userWorkoutPlanId = userWorkoutPlanId;
    }

    public void loadWorkoutPlan(String planId) {
        _isLoading.setValue(true);
        workoutsRepository.getWorkoutPlanById(planId, new WorkoutsRepository.WorkoutsCallback<DetailedWorkoutPlan>() {
            @Override
            public void onSuccess(DetailedWorkoutPlan result) {
                _isLoading.setValue(false);
                _detailedWorkoutPlan.setValue(result);
                _error.setValue(null);
                
                // Load workout plan results if we have userWorkoutPlanId and current user
                if (userWorkoutPlanId != null && _currentUserId.getValue() != null) {
                    loadWorkoutPlanResults();
                }
            }

            @Override
            public void onError(String error) {
                _isLoading.setValue(false);
                _error.setValue(error);
            }
        });
    }

    public void loadWorkoutPlanResults() {
        if (userWorkoutPlanId == null || _currentUserId.getValue() == null) {
            return;
        }

        usersRepository.getUserWorkoutPlanResults(userWorkoutPlanId, _currentUserId.getValue(), 
            new UsersRepository.UsersCallback<WorkoutPlanResults>() {
                @Override
                public void onSuccess(WorkoutPlanResults result) {
                    _workoutPlanResults.setValue(result);
                }

                @Override
                public void onError(String error) {
                    // Don't show error for workout plan results as it's optional
                    _workoutPlanResults.setValue(null);
                }
            });
    }

    public void clearError() {
        _error.setValue(null);
    }

    private void loadCurrentUserId() {
        authRepository.getCurrentUserIdSync()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                userId -> _currentUserId.setValue(userId),
                throwable -> {
                    // Handle error if needed
                    android.util.Log.e("WorkoutPlanDetailsViewModel", "Failed to get current user ID", throwable);
                }
            );
    }
}
