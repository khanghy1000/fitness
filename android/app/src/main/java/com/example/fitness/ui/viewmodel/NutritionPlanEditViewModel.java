package com.example.fitness.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.fitness.data.network.model.generated.BulkUpdateNutritionPlan;
import com.example.fitness.data.network.model.generated.BulkNutritionPlanDay;
import com.example.fitness.data.network.model.generated.BulkNutritionPlanMeal;
import com.example.fitness.data.network.model.generated.BulkNutritionPlanFood;
import com.example.fitness.data.network.model.generated.CreateNutritionPlan;
import com.example.fitness.data.network.model.generated.DetailedNutritionPlan;
import com.example.fitness.data.network.model.generated.DetailedNutritionPlanMeal;
import com.example.fitness.data.network.model.generated.DetailedNutritionPlanFood;
import com.example.fitness.data.network.model.generated.NutritionPlan;
import com.example.fitness.data.network.model.generated.NutritionPlanDay;
import com.example.fitness.data.network.model.generated.NutritionPlanFood;
import com.example.fitness.data.network.model.generated.NutritionPlanMeal;
import com.example.fitness.data.network.model.generated.UpdateNutritionPlan;
import com.example.fitness.data.repository.NutritionRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class NutritionPlanEditViewModel extends ViewModel {
    private final NutritionRepository nutritionRepository;

    private final MutableLiveData<DetailedNutritionPlan> _detailedNutritionPlan = new MutableLiveData<>();
    public final LiveData<DetailedNutritionPlan> detailedNutritionPlan = _detailedNutritionPlan;

    private final MutableLiveData<List<EditablePlanDay>> _editableDays = new MutableLiveData<>();
    public final LiveData<List<EditablePlanDay>> editableDays = _editableDays;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    private final MutableLiveData<Boolean> _isSaving = new MutableLiveData<>();
    public final LiveData<Boolean> isSaving = _isSaving;

    private final MutableLiveData<Boolean> _saveSuccess = new MutableLiveData<>();
    public final LiveData<Boolean> saveSuccess = _saveSuccess;

    private String currentPlanId;
    private boolean isNewPlan = false;
    
    // Temporary storage for new plan info before creation
    private String planName;
    private String planDescription;
    private boolean planIsActive = false;

    @Inject
    public NutritionPlanEditViewModel(NutritionRepository nutritionRepository) {
        this.nutritionRepository = nutritionRepository;
        _editableDays.setValue(new ArrayList<>());
    }

    public void initializeForNewPlan() {
        isNewPlan = true;
        currentPlanId = null;
        _detailedNutritionPlan.setValue(null);
        _editableDays.setValue(new ArrayList<>());
    }

    public void initializeForExistingPlan(String planId) {
        isNewPlan = false;
        currentPlanId = planId;
        loadNutritionPlanForEdit(planId);
    }

    private void loadNutritionPlanForEdit(String planId) {
        _isLoading.setValue(true);
        
        nutritionRepository.getNutritionPlanById(planId, new NutritionRepository.NutritionCallback<DetailedNutritionPlan>() {
            @Override
            public void onSuccess(DetailedNutritionPlan result) {
                _detailedNutritionPlan.setValue(result);
                convertDetailedPlanToEditableFormat(result);
                _isLoading.setValue(false);
            }

            @Override
            public void onError(String error) {
                _isLoading.setValue(false);
                _error.setValue(error);
            }
        });
    }

    private void convertDetailedPlanToEditableFormat(DetailedNutritionPlan detailedPlan) {
        List<EditablePlanDay> editableDays = new ArrayList<>();
        
        for (com.example.fitness.data.network.model.generated.DetailedNutritionPlanDay day : detailedPlan.getDays()) {
            EditablePlanDay editableDay = new EditablePlanDay();
            editableDay.id = day.getId();
            editableDay.weekday = day.getWeekday().getValue();
            editableDay.totalCalories = day.getTotalCalories() != null ? day.getTotalCalories().toString() : "";
            editableDay.protein = day.getProtein() != null ? day.getProtein().toString() : "";
            editableDay.carbs = day.getCarbs() != null ? day.getCarbs().toString() : "";
            editableDay.fat = day.getFat() != null ? day.getFat().toString() : "";
            editableDay.fiber = day.getFiber() != null ? day.getFiber().toString() : "";
            editableDay.meals = new ArrayList<>();
            
            // Convert meals
            for (com.example.fitness.data.network.model.generated.DetailedNutritionPlanMeal meal : day.getMeals()) {
                EditablePlanMeal editableMeal = new EditablePlanMeal();
                editableMeal.id = meal.getId();
                editableMeal.name = meal.getName();
                editableMeal.time = meal.getTime();
                editableMeal.calories = meal.getCalories() != null ? meal.getCalories().toString() : "";
                editableMeal.protein = meal.getProtein() != null ? meal.getProtein().toString() : "";
                editableMeal.carbs = meal.getCarbs() != null ? meal.getCarbs().toString() : "";
                editableMeal.fat = meal.getFat() != null ? meal.getFat().toString() : "";
                editableMeal.fiber = meal.getFiber() != null ? meal.getFiber().toString() : "";
                editableMeal.foods = new ArrayList<>();
                
                // Convert foods
                for (com.example.fitness.data.network.model.generated.DetailedNutritionPlanFood food : meal.getFoods()) {
                    EditablePlanFood editableFood = new EditablePlanFood();
                    editableFood.id = food.getId();
                    editableFood.name = food.getName();
                    editableFood.quantity = food.getQuantity();
                    editableFood.calories = String.valueOf(food.getCalories());
                    editableFood.protein = food.getProtein() != null ? food.getProtein().toString() : "";
                    editableFood.carbs = food.getCarbs() != null ? food.getCarbs().toString() : "";
                    editableFood.fat = food.getFat() != null ? food.getFat().toString() : "";
                    editableFood.fiber = food.getFiber() != null ? food.getFiber().toString() : "";
                    editableMeal.foods.add(editableFood);
                }
                
                editableDay.meals.add(editableMeal);
            }
            
            editableDays.add(editableDay);
        }
        
        _editableDays.setValue(editableDays);
    }

    public void addNewDay() {
        List<EditablePlanDay> currentDays = _editableDays.getValue();
        if (currentDays != null) {
            EditablePlanDay newDay = new EditablePlanDay();
            newDay.meals = new ArrayList<>();
            currentDays.add(newDay);
            _editableDays.setValue(currentDays);
        }
    }
    
    public void removeDay(int index) {
        List<EditablePlanDay> currentDays = _editableDays.getValue();
        if (currentDays != null && index >= 0 && index < currentDays.size()) {
            currentDays.remove(index);
            _editableDays.setValue(currentDays);
        }
    }

    public void addMealToDay(int dayIndex) {
        List<EditablePlanDay> currentDays = _editableDays.getValue();
        if (currentDays != null && dayIndex >= 0 && dayIndex < currentDays.size()) {
            EditablePlanMeal newMeal = new EditablePlanMeal();
            newMeal.foods = new ArrayList<>();
            currentDays.get(dayIndex).meals.add(newMeal);
            _editableDays.setValue(currentDays);
        }
    }

    public void removeMealFromDay(int dayIndex, int mealIndex) {
        List<EditablePlanDay> currentDays = _editableDays.getValue();
        if (currentDays != null && dayIndex >= 0 && dayIndex < currentDays.size()) {
            List<EditablePlanMeal> meals = currentDays.get(dayIndex).meals;
            if (mealIndex >= 0 && mealIndex < meals.size()) {
                meals.remove(mealIndex);
                _editableDays.setValue(currentDays);
            }
        }
    }

    public void addFoodToMeal(int dayIndex, int mealIndex) {
        List<EditablePlanDay> currentDays = _editableDays.getValue();
        if (currentDays != null && dayIndex >= 0 && dayIndex < currentDays.size()) {
            List<EditablePlanMeal> meals = currentDays.get(dayIndex).meals;
            if (mealIndex >= 0 && mealIndex < meals.size()) {
                EditablePlanFood newFood = new EditablePlanFood();
                meals.get(mealIndex).foods.add(newFood);
                _editableDays.setValue(currentDays);
            }
        }
    }

    public void removeFoodFromMeal(int dayIndex, int mealIndex, int foodIndex) {
        List<EditablePlanDay> currentDays = _editableDays.getValue();
        if (currentDays != null && dayIndex >= 0 && dayIndex < currentDays.size()) {
            List<EditablePlanMeal> meals = currentDays.get(dayIndex).meals;
            if (mealIndex >= 0 && mealIndex < meals.size()) {
                List<EditablePlanFood> foods = meals.get(mealIndex).foods;
                if (foodIndex >= 0 && foodIndex < foods.size()) {
                    foods.remove(foodIndex);
                    _editableDays.setValue(currentDays);
                }
            }
        }
    }

    public void savePlan(String name, String description, boolean isActive) {
        _isSaving.setValue(true);
        
        if (isNewPlan) {
            // Use the provided parameters or fall back to stored values
            String finalName = (name != null && !name.trim().isEmpty()) ? name : planName;
            String finalDescription = (description != null && !description.trim().isEmpty()) ? description : planDescription;
            boolean finalIsActive = isActive || planIsActive;
            createNewPlan(finalName, finalDescription);
        } else {
            updateExistingPlan(name, description, isActive);
        }
    }

    private void createNewPlan(String name, String description) {
        CreateNutritionPlan createRequest = new CreateNutritionPlan(name, description);
        
        nutritionRepository.createNutritionPlan(createRequest, new NutritionRepository.NutritionCallback<NutritionPlan>() {
            @Override
            public void onSuccess(NutritionPlan result) {
                currentPlanId = String.valueOf(result.getId());
                isNewPlan = false; // Now it's no longer a new plan
                
                // If there are days to save, use bulk update, otherwise just finish
                List<EditablePlanDay> editableDays = _editableDays.getValue();
                if (editableDays != null && !editableDays.isEmpty()) {
                    saveDays();
                } else {
                    _isSaving.setValue(false);
                    _saveSuccess.setValue(true);
                }
            }

            @Override
            public void onError(String error) {
                _isSaving.setValue(false);
                _error.setValue(error);
            }
        });
    }

    private void updateExistingPlan(String name, String description, boolean isActive) {
        // Use bulk update to update both plan details and days
        List<EditablePlanDay> editableDays = _editableDays.getValue();
        if (editableDays == null) {
            editableDays = new ArrayList<>();
        }
        
        try {
            List<BulkNutritionPlanDay> bulkDays = convertEditableDaysToBulkDays(editableDays);
            BulkUpdateNutritionPlan bulkUpdate = new BulkUpdateNutritionPlan(
                name, 
                description, 
                isActive, 
                bulkDays
            );
            
            nutritionRepository.bulkUpdateNutritionPlan(currentPlanId, bulkUpdate, new NutritionRepository.NutritionCallback<DetailedNutritionPlan>() {
                @Override
                public void onSuccess(DetailedNutritionPlan result) {
                    _isSaving.setValue(false);
                    _saveSuccess.setValue(true);
                    // Update the current data with the response
                    _detailedNutritionPlan.setValue(result);
                    convertDetailedPlanToEditableFormat(result);
                }

                @Override
                public void onError(String error) {
                    _isSaving.setValue(false);
                    _error.setValue("Failed to save nutrition plan: " + error);
                }
            });
        } catch (Exception e) {
            _isSaving.setValue(false);
            _error.setValue("Error preparing data for save: " + e.getMessage());
        }
    }

    private void saveDays() {
        // Use bulk update for existing plans
        List<EditablePlanDay> editableDays = _editableDays.getValue();
        if (editableDays == null) {
            _isSaving.setValue(false);
            _error.setValue("No days to save");
            return;
        }
        
        try {
            List<BulkNutritionPlanDay> bulkDays = convertEditableDaysToBulkDays(editableDays);
            BulkUpdateNutritionPlan bulkUpdate = new BulkUpdateNutritionPlan(null, null, null, bulkDays);
            
            nutritionRepository.bulkUpdateNutritionPlan(currentPlanId, bulkUpdate, new NutritionRepository.NutritionCallback<DetailedNutritionPlan>() {
                @Override
                public void onSuccess(DetailedNutritionPlan result) {
                    _isSaving.setValue(false);
                    _saveSuccess.setValue(true);
                    // Update the current data with the response
                    _detailedNutritionPlan.setValue(result);
                    convertDetailedPlanToEditableFormat(result);
                }

                @Override
                public void onError(String error) {
                    _isSaving.setValue(false);
                    _error.setValue("Failed to save nutrition plan: " + error);
                }
            });
        } catch (Exception e) {
            _isSaving.setValue(false);
            _error.setValue("Error preparing data for save: " + e.getMessage());
        }
    }
    
    private List<BulkNutritionPlanDay> convertEditableDaysToBulkDays(List<EditablePlanDay> editableDays) {
        List<BulkNutritionPlanDay> bulkDays = new ArrayList<>();
        
        for (EditablePlanDay editableDay : editableDays) {
            // Skip days without a weekday
            if (editableDay.weekday == null || editableDay.weekday.trim().isEmpty()) {
                continue;
            }
            
            BulkNutritionPlanDay.Weekday weekday;
            try {
                weekday = BulkNutritionPlanDay.Weekday.valueOf(editableDay.weekday.toLowerCase());
            } catch (IllegalArgumentException e) {
                continue; // Skip invalid weekdays
            }
            
            List<BulkNutritionPlanMeal> bulkMeals = new ArrayList<>();
            for (EditablePlanMeal editableMeal : editableDay.meals) {
                // Skip meals without name or time
                if (editableMeal.name == null || editableMeal.name.trim().isEmpty() ||
                    editableMeal.time == null || editableMeal.time.trim().isEmpty()) {
                    continue;
                }
                
                List<BulkNutritionPlanFood> bulkFoods = new ArrayList<>();
                for (EditablePlanFood editableFood : editableMeal.foods) {
                    // Skip foods without name, quantity, or calories
                    if (editableFood.name == null || editableFood.name.trim().isEmpty() ||
                        editableFood.quantity == null || editableFood.quantity.trim().isEmpty() ||
                        editableFood.calories == null || editableFood.calories.trim().isEmpty()) {
                        continue;
                    }
                    
                    try {
                        int calories = Integer.parseInt(editableFood.calories.trim());
                        BigDecimal protein = parseNullableBigDecimal(editableFood.protein);
                        BigDecimal carbs = parseNullableBigDecimal(editableFood.carbs);
                        BigDecimal fat = parseNullableBigDecimal(editableFood.fat);
                        BigDecimal fiber = parseNullableBigDecimal(editableFood.fiber);
                        
                        BulkNutritionPlanFood bulkFood = new BulkNutritionPlanFood(
                            editableFood.name.trim(),
                            editableFood.quantity.trim(),
                            calories,
                            editableFood.id,
                            protein,
                            carbs,
                            fat,
                            fiber
                        );
                        bulkFoods.add(bulkFood);
                    } catch (NumberFormatException e) {
                        // Skip foods with invalid numeric values
                        continue;
                    }
                }
                
                try {
                    Integer calories = parseNullableInteger(editableMeal.calories);
                    BigDecimal protein = parseNullableBigDecimal(editableMeal.protein);
                    BigDecimal carbs = parseNullableBigDecimal(editableMeal.carbs);
                    BigDecimal fat = parseNullableBigDecimal(editableMeal.fat);
                    BigDecimal fiber = parseNullableBigDecimal(editableMeal.fiber);
                    
                    BulkNutritionPlanMeal bulkMeal = new BulkNutritionPlanMeal(
                        editableMeal.name.trim(),
                        editableMeal.time.trim(),
                        editableMeal.id,
                        calories,
                        protein,
                        carbs,
                        fat,
                        fiber,
                        bulkFoods.isEmpty() ? null : bulkFoods
                    );
                    bulkMeals.add(bulkMeal);
                } catch (NumberFormatException e) {
                    // Skip meals with invalid numeric values
                    continue;
                }
            }
            
            try {
                Integer totalCalories = parseNullableInteger(editableDay.totalCalories);
                BigDecimal protein = parseNullableBigDecimal(editableDay.protein);
                BigDecimal carbs = parseNullableBigDecimal(editableDay.carbs);
                BigDecimal fat = parseNullableBigDecimal(editableDay.fat);
                BigDecimal fiber = parseNullableBigDecimal(editableDay.fiber);
                
                BulkNutritionPlanDay bulkDay = new BulkNutritionPlanDay(
                    weekday,
                    editableDay.id,
                    totalCalories,
                    protein,
                    carbs,
                    fat,
                    fiber,
                    bulkMeals.isEmpty() ? null : bulkMeals
                );
                bulkDays.add(bulkDay);
            } catch (NumberFormatException e) {
                // Skip days with invalid numeric values
                continue;
            }
        }
        
        return bulkDays;
    }
    
    private Integer parseNullableInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return Integer.parseInt(value.trim());
    }
    
    private BigDecimal parseNullableBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return new BigDecimal(value.trim());
    }

    public void clearError() {
        _error.setValue(null);
    }

    public void clearSaveSuccess() {
        _saveSuccess.setValue(false);
    }

    // Data classes for editable items
    public static class EditablePlanDay {
        public Integer id;
        public String weekday = "";
        public String totalCalories = "";
        public String protein = "";
        public String carbs = "";
        public String fat = "";
        public String fiber = "";
        public List<EditablePlanMeal> meals = new ArrayList<>();
    }

    public static class EditablePlanMeal {
        public Integer id;
        public String name = "";
        public String time = "";
        public String calories = "";
        public String protein = "";
        public String carbs = "";
        public String fat = "";
        public String fiber = "";
        public List<EditablePlanFood> foods = new ArrayList<>();
    }

    public static class EditablePlanFood {
        public Integer id;
        public String name = "";
        public String quantity = "";
        public String calories = "";
        public String protein = "";
        public String carbs = "";
        public String fat = "";
        public String fiber = "";
    }
    
    public void updateDayWeekday(int dayIndex, String weekday) {
        List<EditablePlanDay> currentDays = _editableDays.getValue();
        if (currentDays != null && dayIndex >= 0 && dayIndex < currentDays.size()) {
            // Check if weekday is already used by another day
            for (int i = 0; i < currentDays.size(); i++) {
                if (i != dayIndex && weekday.equals(currentDays.get(i).weekday)) {
                    _error.setValue("This weekday is already used by another day. Each weekday can only be used once per plan.");
                    return;
                }
            }
            currentDays.get(dayIndex).weekday = weekday;
            _editableDays.setValue(currentDays);
        }
    }
    
    // Day update methods
    public void updateDayCalories(int dayIndex, String calories) {
        List<EditablePlanDay> currentDays = _editableDays.getValue();
        if (currentDays != null && dayIndex >= 0 && dayIndex < currentDays.size()) {
            currentDays.get(dayIndex).totalCalories = calories;
            _editableDays.postValue(currentDays);
        }
    }
    
    public void updateDayProtein(int dayIndex, String protein) {
        List<EditablePlanDay> currentDays = _editableDays.getValue();
        if (currentDays != null && dayIndex >= 0 && dayIndex < currentDays.size()) {
            currentDays.get(dayIndex).protein = protein;
            _editableDays.postValue(currentDays);
        }
    }
    
    public void updateDayCarbs(int dayIndex, String carbs) {
        List<EditablePlanDay> currentDays = _editableDays.getValue();
        if (currentDays != null && dayIndex >= 0 && dayIndex < currentDays.size()) {
            currentDays.get(dayIndex).carbs = carbs;
            _editableDays.postValue(currentDays);
        }
    }
    
    public void updateDayFat(int dayIndex, String fat) {
        List<EditablePlanDay> currentDays = _editableDays.getValue();
        if (currentDays != null && dayIndex >= 0 && dayIndex < currentDays.size()) {
            currentDays.get(dayIndex).fat = fat;
            _editableDays.postValue(currentDays);
        }
    }
    
    public void updateDayFiber(int dayIndex, String fiber) {
        List<EditablePlanDay> currentDays = _editableDays.getValue();
        if (currentDays != null && dayIndex >= 0 && dayIndex < currentDays.size()) {
            currentDays.get(dayIndex).fiber = fiber;
            _editableDays.postValue(currentDays);
        }
    }
    
    // Meal update methods
    public void updateMealName(int dayIndex, int mealIndex, String name) {
        List<EditablePlanDay> currentDays = _editableDays.getValue();
        if (currentDays != null && dayIndex >= 0 && dayIndex < currentDays.size()) {
            List<EditablePlanMeal> meals = currentDays.get(dayIndex).meals;
            if (mealIndex >= 0 && mealIndex < meals.size()) {
                meals.get(mealIndex).name = name;
                _editableDays.postValue(currentDays);
            }
        }
    }
    
    public void updateMealTime(int dayIndex, int mealIndex, String time) {
        List<EditablePlanDay> currentDays = _editableDays.getValue();
        if (currentDays != null && dayIndex >= 0 && dayIndex < currentDays.size()) {
            List<EditablePlanMeal> meals = currentDays.get(dayIndex).meals;
            if (mealIndex >= 0 && mealIndex < meals.size()) {
                meals.get(mealIndex).time = time;
                _editableDays.postValue(currentDays);
            }
        }
    }
    
    public void updateMealCalories(int dayIndex, int mealIndex, String calories) {
        List<EditablePlanDay> currentDays = _editableDays.getValue();
        if (currentDays != null && dayIndex >= 0 && dayIndex < currentDays.size()) {
            List<EditablePlanMeal> meals = currentDays.get(dayIndex).meals;
            if (mealIndex >= 0 && mealIndex < meals.size()) {
                meals.get(mealIndex).calories = calories;
                _editableDays.postValue(currentDays);
            }
        }
    }
    
    public void updateMealProtein(int dayIndex, int mealIndex, String protein) {
        List<EditablePlanDay> currentDays = _editableDays.getValue();
        if (currentDays != null && dayIndex >= 0 && dayIndex < currentDays.size()) {
            List<EditablePlanMeal> meals = currentDays.get(dayIndex).meals;
            if (mealIndex >= 0 && mealIndex < meals.size()) {
                meals.get(mealIndex).protein = protein;
                _editableDays.postValue(currentDays);
            }
        }
    }
    
    public void updateMealCarbs(int dayIndex, int mealIndex, String carbs) {
        List<EditablePlanDay> currentDays = _editableDays.getValue();
        if (currentDays != null && dayIndex >= 0 && dayIndex < currentDays.size()) {
            List<EditablePlanMeal> meals = currentDays.get(dayIndex).meals;
            if (mealIndex >= 0 && mealIndex < meals.size()) {
                meals.get(mealIndex).carbs = carbs;
                _editableDays.postValue(currentDays);
            }
        }
    }
    
    public void updateMealFat(int dayIndex, int mealIndex, String fat) {
        List<EditablePlanDay> currentDays = _editableDays.getValue();
        if (currentDays != null && dayIndex >= 0 && dayIndex < currentDays.size()) {
            List<EditablePlanMeal> meals = currentDays.get(dayIndex).meals;
            if (mealIndex >= 0 && mealIndex < meals.size()) {
                meals.get(mealIndex).fat = fat;
                _editableDays.postValue(currentDays);
            }
        }
    }
    
    public void updateMealFiber(int dayIndex, int mealIndex, String fiber) {
        List<EditablePlanDay> currentDays = _editableDays.getValue();
        if (currentDays != null && dayIndex >= 0 && dayIndex < currentDays.size()) {
            List<EditablePlanMeal> meals = currentDays.get(dayIndex).meals;
            if (mealIndex >= 0 && mealIndex < meals.size()) {
                meals.get(mealIndex).fiber = fiber;
                _editableDays.postValue(currentDays);
            }
        }
    }
    
    // Food update methods
    public void updateFoodName(int dayIndex, int mealIndex, int foodIndex, String name) {
        List<EditablePlanDay> currentDays = _editableDays.getValue();
        if (currentDays != null && dayIndex >= 0 && dayIndex < currentDays.size()) {
            List<EditablePlanMeal> meals = currentDays.get(dayIndex).meals;
            if (mealIndex >= 0 && mealIndex < meals.size()) {
                List<EditablePlanFood> foods = meals.get(mealIndex).foods;
                if (foodIndex >= 0 && foodIndex < foods.size()) {
                    foods.get(foodIndex).name = name;
                    _editableDays.postValue(currentDays);
                }
            }
        }
    }
    
    public void updateFoodQuantity(int dayIndex, int mealIndex, int foodIndex, String quantity) {
        List<EditablePlanDay> currentDays = _editableDays.getValue();
        if (currentDays != null && dayIndex >= 0 && dayIndex < currentDays.size()) {
            List<EditablePlanMeal> meals = currentDays.get(dayIndex).meals;
            if (mealIndex >= 0 && mealIndex < meals.size()) {
                List<EditablePlanFood> foods = meals.get(mealIndex).foods;
                if (foodIndex >= 0 && foodIndex < foods.size()) {
                    foods.get(foodIndex).quantity = quantity;
                    _editableDays.postValue(currentDays);
                }
            }
        }
    }
    
    public void updateFoodCalories(int dayIndex, int mealIndex, int foodIndex, String calories) {
        List<EditablePlanDay> currentDays = _editableDays.getValue();
        if (currentDays != null && dayIndex >= 0 && dayIndex < currentDays.size()) {
            List<EditablePlanMeal> meals = currentDays.get(dayIndex).meals;
            if (mealIndex >= 0 && mealIndex < meals.size()) {
                List<EditablePlanFood> foods = meals.get(mealIndex).foods;
                if (foodIndex >= 0 && foodIndex < foods.size()) {
                    foods.get(foodIndex).calories = calories;
                    _editableDays.postValue(currentDays);
                }
            }
        }
    }
    
    public void updateFoodProtein(int dayIndex, int mealIndex, int foodIndex, String protein) {
        List<EditablePlanDay> currentDays = _editableDays.getValue();
        if (currentDays != null && dayIndex >= 0 && dayIndex < currentDays.size()) {
            List<EditablePlanMeal> meals = currentDays.get(dayIndex).meals;
            if (mealIndex >= 0 && mealIndex < meals.size()) {
                List<EditablePlanFood> foods = meals.get(mealIndex).foods;
                if (foodIndex >= 0 && foodIndex < foods.size()) {
                    foods.get(foodIndex).protein = protein;
                    _editableDays.postValue(currentDays);
                }
            }
        }
    }
    
    public void updateFoodCarbs(int dayIndex, int mealIndex, int foodIndex, String carbs) {
        List<EditablePlanDay> currentDays = _editableDays.getValue();
        if (currentDays != null && dayIndex >= 0 && dayIndex < currentDays.size()) {
            List<EditablePlanMeal> meals = currentDays.get(dayIndex).meals;
            if (mealIndex >= 0 && mealIndex < meals.size()) {
                List<EditablePlanFood> foods = meals.get(mealIndex).foods;
                if (foodIndex >= 0 && foodIndex < foods.size()) {
                    foods.get(foodIndex).carbs = carbs;
                    _editableDays.postValue(currentDays);
                }
            }
        }
    }
    
    public void updateFoodFat(int dayIndex, int mealIndex, int foodIndex, String fat) {
        List<EditablePlanDay> currentDays = _editableDays.getValue();
        if (currentDays != null && dayIndex >= 0 && dayIndex < currentDays.size()) {
            List<EditablePlanMeal> meals = currentDays.get(dayIndex).meals;
            if (mealIndex >= 0 && mealIndex < meals.size()) {
                List<EditablePlanFood> foods = meals.get(mealIndex).foods;
                if (foodIndex >= 0 && foodIndex < foods.size()) {
                    foods.get(foodIndex).fat = fat;
                    _editableDays.postValue(currentDays);
                }
            }
        }
    }
    
    public void updateFoodFiber(int dayIndex, int mealIndex, int foodIndex, String fiber) {
        List<EditablePlanDay> currentDays = _editableDays.getValue();
        if (currentDays != null && dayIndex >= 0 && dayIndex < currentDays.size()) {
            List<EditablePlanMeal> meals = currentDays.get(dayIndex).meals;
            if (mealIndex >= 0 && mealIndex < meals.size()) {
                List<EditablePlanFood> foods = meals.get(mealIndex).foods;
                if (foodIndex >= 0 && foodIndex < foods.size()) {
                    foods.get(foodIndex).fiber = fiber;
                    _editableDays.postValue(currentDays);
                }
            }
        }
    }
    
    public List<String> getAvailableWeekdays() {
        List<String> allWeekdays = java.util.Arrays.asList("sun", "mon", "tue", "wed", "thu", "fri", "sat");
        List<String> usedWeekdays = new ArrayList<>();
        
        List<EditablePlanDay> currentDays = _editableDays.getValue();
        if (currentDays != null) {
            for (EditablePlanDay day : currentDays) {
                if (day.weekday != null && !day.weekday.trim().isEmpty()) {
                    usedWeekdays.add(day.weekday);
                }
            }
        }
        
        List<String> availableWeekdays = new ArrayList<>();
        for (String weekday : allWeekdays) {
            if (!usedWeekdays.contains(weekday)) {
                availableWeekdays.add(weekday);
            }
        }
        
        return availableWeekdays;
    }

    public void updatePlanInfo(String name, String description, boolean isActive) {
        DetailedNutritionPlan currentPlan = _detailedNutritionPlan.getValue();
        if (currentPlan != null) {
            // Since DetailedNutritionPlan is a Kotlin data class with immutable properties,
            // we need to create a new instance with updated values using copy()
            DetailedNutritionPlan updatedPlan = currentPlan.copy(
                currentPlan.getId(),
                name,
                currentPlan.getCreatedBy(),
                isActive,
                currentPlan.getCreatedAt(),
                currentPlan.getUpdatedAt(),
                currentPlan.getDays(),
                description
            );
            _detailedNutritionPlan.setValue(updatedPlan);
        } else {
            // For new plans, we'll just store the values and create the plan object when saving
            // Since we can't create a DetailedNutritionPlan without all required fields,
            // we'll create a temporary storage mechanism
            planName = name;
            planDescription = description;
            planIsActive = isActive;
        }
    }

    public EditablePlanDay getDayAt(int dayIndex) {
        List<EditablePlanDay> currentDays = _editableDays.getValue();
        if (currentDays != null && dayIndex >= 0 && dayIndex < currentDays.size()) {
            return currentDays.get(dayIndex);
        }
        return null;
    }

    public EditablePlanMeal getMealAt(int dayIndex, int mealIndex) {
        EditablePlanDay day = getDayAt(dayIndex);
        if (day != null && day.meals != null && mealIndex >= 0 && mealIndex < day.meals.size()) {
            return day.meals.get(mealIndex);
        }
        return null;
    }

    public EditablePlanFood getFoodAt(int dayIndex, int mealIndex, int foodIndex) {
        EditablePlanMeal meal = getMealAt(dayIndex, mealIndex);
        if (meal != null && meal.foods != null && foodIndex >= 0 && foodIndex < meal.foods.size()) {
            return meal.foods.get(foodIndex);
        }
        return null;
    }

    public String getCurrentPlanName() {
        DetailedNutritionPlan currentPlan = _detailedNutritionPlan.getValue();
        if (currentPlan != null) {
            return currentPlan.getName();
        }
        return planName != null ? planName : "";
    }

    public String getCurrentPlanDescription() {
        DetailedNutritionPlan currentPlan = _detailedNutritionPlan.getValue();
        if (currentPlan != null) {
            return currentPlan.getDescription();
        }
        return planDescription != null ? planDescription : "";
    }

    public boolean getCurrentPlanIsActive() {
        DetailedNutritionPlan currentPlan = _detailedNutritionPlan.getValue();
        if (currentPlan != null) {
            return currentPlan.isActive();
        }
        return planIsActive;
    }
}
