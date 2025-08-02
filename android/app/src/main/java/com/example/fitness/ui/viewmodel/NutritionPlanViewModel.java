package com.example.fitness.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.fitness.data.network.model.generated.CreateNutritionPlan;
import com.example.fitness.data.network.model.generated.NutritionPlan;
import com.example.fitness.data.repository.NutritionRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class NutritionPlanViewModel extends ViewModel {
    private final NutritionRepository nutritionRepository;

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

    @Inject
    public NutritionPlanViewModel(NutritionRepository nutritionRepository) {
        this.nutritionRepository = nutritionRepository;
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
}
