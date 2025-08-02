package com.example.fitness.data.repository;

import com.example.fitness.data.network.retrofit.NutritionApi;
import com.example.fitness.data.network.model.generated.*;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class NutritionRepository {
    private final NutritionApi nutritionApi;

    @Inject
    public NutritionRepository(NutritionApi nutritionApi) {
        this.nutritionApi = nutritionApi;
    }

    public interface NutritionCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public void getAllNutritionPlans(NutritionCallback<java.util.List<NutritionPlan>> callback) {
        nutritionApi.apiNutritionGet().enqueue(new Callback<java.util.List<NutritionPlan>>() {
            @Override
            public void onResponse(Call<java.util.List<NutritionPlan>> call, Response<java.util.List<NutritionPlan>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to get nutrition plans: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<java.util.List<NutritionPlan>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void createNutritionPlan(CreateNutritionPlan createNutritionPlan, NutritionCallback<NutritionPlan> callback) {
        nutritionApi.apiNutritionPost(createNutritionPlan).enqueue(new Callback<NutritionPlan>() {
            @Override
            public void onResponse(Call<NutritionPlan> call, Response<NutritionPlan> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to create nutrition plan: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<NutritionPlan> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getNutritionPlanById(String id, NutritionCallback<DetailedNutritionPlan> callback) {
        nutritionApi.apiNutritionIdGet(id).enqueue(new Callback<DetailedNutritionPlan>() {
            @Override
            public void onResponse(Call<DetailedNutritionPlan> call, Response<DetailedNutritionPlan> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to get nutrition plan: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<DetailedNutritionPlan> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void updateNutritionPlan(String id, UpdateNutritionPlan updateNutritionPlan, NutritionCallback<NutritionPlan> callback) {
        nutritionApi.apiNutritionIdPut(id, updateNutritionPlan).enqueue(new Callback<NutritionPlan>() {
            @Override
            public void onResponse(Call<NutritionPlan> call, Response<NutritionPlan> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to update nutrition plan: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<NutritionPlan> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void deleteNutritionPlan(String id, NutritionCallback<SuccessMessage> callback) {
        nutritionApi.apiNutritionIdDelete(id).enqueue(new Callback<SuccessMessage>() {
            @Override
            public void onResponse(Call<SuccessMessage> call, Response<SuccessMessage> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to delete nutrition plan: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<SuccessMessage> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getNutritionPlanDays(String id, NutritionCallback<java.util.List<NutritionPlanDay>> callback) {
        nutritionApi.apiNutritionIdDaysGet(id).enqueue(new Callback<java.util.List<NutritionPlanDay>>() {
            @Override
            public void onResponse(Call<java.util.List<NutritionPlanDay>> call, Response<java.util.List<NutritionPlanDay>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to get nutrition plan days: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<java.util.List<NutritionPlanDay>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void createNutritionPlanDay(String id, CreateNutritionPlanDay createNutritionPlanDay, NutritionCallback<NutritionPlanDay> callback) {
        nutritionApi.apiNutritionIdDaysPost(id, createNutritionPlanDay).enqueue(new Callback<NutritionPlanDay>() {
            @Override
            public void onResponse(Call<NutritionPlanDay> call, Response<NutritionPlanDay> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to create nutrition plan day: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<NutritionPlanDay> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getNutritionPlanDay(String id, NutritionCallback<NutritionPlanDay> callback) {
        nutritionApi.apiNutritionDaysIdGet(id).enqueue(new Callback<NutritionPlanDay>() {
            @Override
            public void onResponse(Call<NutritionPlanDay> call, Response<NutritionPlanDay> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to get nutrition plan day: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<NutritionPlanDay> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void updateNutritionPlanDay(String id, UpdateNutritionPlanDay updateNutritionPlanDay, NutritionCallback<NutritionPlanDay> callback) {
        nutritionApi.apiNutritionDaysIdPut(id, updateNutritionPlanDay).enqueue(new Callback<NutritionPlanDay>() {
            @Override
            public void onResponse(Call<NutritionPlanDay> call, Response<NutritionPlanDay> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to update nutrition plan day: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<NutritionPlanDay> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void deleteNutritionPlanDay(String id, NutritionCallback<SuccessMessage> callback) {
        nutritionApi.apiNutritionDaysIdDelete(id).enqueue(new Callback<SuccessMessage>() {
            @Override
            public void onResponse(Call<SuccessMessage> call, Response<SuccessMessage> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to delete nutrition plan day: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<SuccessMessage> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getNutritionPlanDayMeals(String id, NutritionCallback<java.util.List<NutritionPlanMeal>> callback) {
        nutritionApi.apiNutritionDaysIdMealsGet(id).enqueue(new Callback<java.util.List<NutritionPlanMeal>>() {
            @Override
            public void onResponse(Call<java.util.List<NutritionPlanMeal>> call, Response<java.util.List<NutritionPlanMeal>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to get nutrition plan day meals: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<java.util.List<NutritionPlanMeal>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void createNutritionPlanMeal(String id, CreateNutritionPlanMeal createNutritionPlanMeal, NutritionCallback<NutritionPlanMeal> callback) {
        nutritionApi.apiNutritionDaysIdMealsPost(id, createNutritionPlanMeal).enqueue(new Callback<NutritionPlanMeal>() {
            @Override
            public void onResponse(Call<NutritionPlanMeal> call, Response<NutritionPlanMeal> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to create nutrition plan meal: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<NutritionPlanMeal> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Meal CRUD operations
    public void getNutritionPlanMeal(String id, NutritionCallback<NutritionPlanMeal> callback) {
        nutritionApi.apiNutritionMealsIdGet(id).enqueue(new Callback<NutritionPlanMeal>() {
            @Override
            public void onResponse(Call<NutritionPlanMeal> call, Response<NutritionPlanMeal> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to get nutrition plan meal: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<NutritionPlanMeal> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void updateNutritionPlanMeal(String id, UpdateNutritionPlanMeal updateNutritionPlanMeal, NutritionCallback<NutritionPlanMeal> callback) {
        nutritionApi.apiNutritionMealsIdPut(id, updateNutritionPlanMeal).enqueue(new Callback<NutritionPlanMeal>() {
            @Override
            public void onResponse(Call<NutritionPlanMeal> call, Response<NutritionPlanMeal> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to update nutrition plan meal: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<NutritionPlanMeal> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void deleteNutritionPlanMeal(String id, NutritionCallback<SuccessMessage> callback) {
        nutritionApi.apiNutritionMealsIdDelete(id).enqueue(new Callback<SuccessMessage>() {
            @Override
            public void onResponse(Call<SuccessMessage> call, Response<SuccessMessage> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to delete nutrition plan meal: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<SuccessMessage> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getNutritionPlanMealFoods(String id, NutritionCallback<java.util.List<NutritionPlanFood>> callback) {
        nutritionApi.apiNutritionMealsIdFoodsGet(id).enqueue(new Callback<java.util.List<NutritionPlanFood>>() {
            @Override
            public void onResponse(Call<java.util.List<NutritionPlanFood>> call, Response<java.util.List<NutritionPlanFood>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to get nutrition plan meal foods: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<java.util.List<NutritionPlanFood>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void createNutritionPlanFood(String id, CreateNutritionPlanFood createNutritionPlanFood, NutritionCallback<NutritionPlanFood> callback) {
        nutritionApi.apiNutritionMealsIdFoodsPost(id, createNutritionPlanFood).enqueue(new Callback<NutritionPlanFood>() {
            @Override
            public void onResponse(Call<NutritionPlanFood> call, Response<NutritionPlanFood> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to create nutrition plan food: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<NutritionPlanFood> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Food CRUD operations
    public void getNutritionPlanFood(String id, NutritionCallback<NutritionPlanFood> callback) {
        nutritionApi.apiNutritionFoodsIdGet(id).enqueue(new Callback<NutritionPlanFood>() {
            @Override
            public void onResponse(Call<NutritionPlanFood> call, Response<NutritionPlanFood> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to get nutrition plan food: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<NutritionPlanFood> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void updateNutritionPlanFood(String id, UpdateNutritionPlanFood updateNutritionPlanFood, NutritionCallback<NutritionPlanFood> callback) {
        nutritionApi.apiNutritionFoodsIdPut(id, updateNutritionPlanFood).enqueue(new Callback<NutritionPlanFood>() {
            @Override
            public void onResponse(Call<NutritionPlanFood> call, Response<NutritionPlanFood> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to update nutrition plan food: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<NutritionPlanFood> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void deleteNutritionPlanFood(String id, NutritionCallback<SuccessMessage> callback) {
        nutritionApi.apiNutritionFoodsIdDelete(id).enqueue(new Callback<SuccessMessage>() {
            @Override
            public void onResponse(Call<SuccessMessage> call, Response<SuccessMessage> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to delete nutrition plan food: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<SuccessMessage> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void bulkUpdateNutritionPlan(String id, BulkUpdateNutritionPlan bulkUpdateNutritionPlan, NutritionCallback<DetailedNutritionPlan> callback) {
        nutritionApi.apiNutritionIdBulkPut(id, bulkUpdateNutritionPlan).enqueue(new Callback<DetailedNutritionPlan>() {
            @Override
            public void onResponse(Call<DetailedNutritionPlan> call, Response<DetailedNutritionPlan> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to bulk update nutrition plan: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<DetailedNutritionPlan> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}
