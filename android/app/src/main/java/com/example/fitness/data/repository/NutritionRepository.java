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
