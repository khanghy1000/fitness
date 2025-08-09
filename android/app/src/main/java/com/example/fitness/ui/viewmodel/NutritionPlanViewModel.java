package com.example.fitness.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.fitness.data.network.model.generated.CreateNutritionPlan;
import com.example.fitness.data.network.model.generated.NutritionPlan;
import com.example.fitness.data.network.model.generated.AssignNutritionPlan;
import com.example.fitness.data.network.model.generated.NutritionPlanAssignmentResponse;
import com.example.fitness.data.repository.NutritionRepository;
import com.example.fitness.data.repository.UsersRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class NutritionPlanViewModel extends ViewModel {
    private final NutritionRepository nutritionRepository;
    private final UsersRepository usersRepository;

    private final MutableLiveData<List<NutritionPlan>> _nutritionPlans = new MutableLiveData<>();
    public final LiveData<List<NutritionPlan>> nutritionPlans = _nutritionPlans;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    private final MutableLiveData<Boolean> _isRefreshing = new MutableLiveData<>();
    public final LiveData<Boolean> isRefreshing = _isRefreshing;

    private final MutableLiveData<NutritionPlan> _createdPlan = new MutableLiveData<>();
    public final LiveData<NutritionPlan> createdPlan = _createdPlan;

    private final MutableLiveData<String> _successMessage = new MutableLiveData<>();
    public final LiveData<String> successMessage = _successMessage;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<String> errorMessage = _errorMessage;

    @Inject
    public NutritionPlanViewModel(NutritionRepository nutritionRepository, UsersRepository usersRepository) {
        this.nutritionRepository = nutritionRepository;
        this.usersRepository = usersRepository;
        loadNutritionPlans();
    }

    public void loadNutritionPlans() {
        _isLoading.setValue(true);
        nutritionRepository.getAllNutritionPlans(new NutritionRepository.NutritionCallback<List<NutritionPlan>>() {
            @Override
            public void onSuccess(List<NutritionPlan> result) {
                _isLoading.setValue(false);
                _isRefreshing.setValue(false);
                _nutritionPlans.setValue(result);
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

    public void refreshNutritionPlans() {
        _isRefreshing.setValue(true);
        loadNutritionPlans();
    }

    public void createNutritionPlan(String name, String description) {
        _isLoading.setValue(true);
        CreateNutritionPlan createRequest = new CreateNutritionPlan(name, description);
        
        nutritionRepository.createNutritionPlan(createRequest, new NutritionRepository.NutritionCallback<NutritionPlan>() {
            @Override
            public void onSuccess(NutritionPlan result) {
                _isLoading.setValue(false);
                _createdPlan.setValue(result);
                _error.setValue(null);
                // Refresh the list
                loadNutritionPlans();
            }

            @Override
            public void onError(String error) {
                _isLoading.setValue(false);
                _error.setValue(error);
            }
        });
    }

    public void deleteNutritionPlan(String planId) {
        _isLoading.setValue(true);
        nutritionRepository.deleteNutritionPlan(planId, new NutritionRepository.NutritionCallback<com.example.fitness.data.network.model.generated.SuccessMessage>() {
            @Override
            public void onSuccess(com.example.fitness.data.network.model.generated.SuccessMessage result) {
                _isLoading.setValue(false);
                _error.setValue(null);
                // Refresh the list
                loadNutritionPlans();
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

    public void assignNutritionPlan(String nutritionPlanId, String traineeId) {
        _isLoading.setValue(true);
        
        // Use current date as start date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String startDate = dateFormat.format(new Date());
        AssignNutritionPlan assignNutritionPlan = new AssignNutritionPlan(traineeId, startDate, null);
        
        usersRepository.assignNutritionPlan(nutritionPlanId, assignNutritionPlan, new UsersRepository.UsersCallback<NutritionPlanAssignmentResponse>() {
            @Override
            public void onSuccess(NutritionPlanAssignmentResponse result) {
                _isLoading.setValue(false);
                _successMessage.setValue("Nutrition plan assigned successfully!");
                _errorMessage.setValue(null);
            }

            @Override
            public void onError(String error) {
                _isLoading.setValue(false);
                _errorMessage.setValue(error);
                _successMessage.setValue(null);
            }
        });
    }

    public void clearMessages() {
        _successMessage.setValue(null);
        _errorMessage.setValue(null);
    }
}
