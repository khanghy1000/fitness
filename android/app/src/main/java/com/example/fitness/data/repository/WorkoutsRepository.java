package com.example.fitness.data.repository;

import com.example.fitness.data.network.retrofit.WorkoutsApi;
import com.example.fitness.data.network.model.generated.*;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class WorkoutsRepository {
    private final WorkoutsApi workoutsApi;

    @Inject
    public WorkoutsRepository(WorkoutsApi workoutsApi) {
        this.workoutsApi = workoutsApi;
    }

    public interface WorkoutsCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public void getAllWorkoutPlans(WorkoutsCallback<java.util.List<WorkoutPlan>> callback) {
        workoutsApi.apiWorkoutsGet().enqueue(new Callback<java.util.List<WorkoutPlan>>() {
            @Override
            public void onResponse(Call<java.util.List<WorkoutPlan>> call, Response<java.util.List<WorkoutPlan>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to get workout plans: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<java.util.List<WorkoutPlan>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void createWorkoutPlan(CreateWorkoutPlan createWorkoutPlan, WorkoutsCallback<WorkoutPlan> callback) {
        workoutsApi.apiWorkoutsPost(createWorkoutPlan).enqueue(new Callback<WorkoutPlan>() {
            @Override
            public void onResponse(Call<WorkoutPlan> call, Response<WorkoutPlan> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to create workout plan: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<WorkoutPlan> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getWorkoutPlanById(String id, WorkoutsCallback<DetailedWorkoutPlan> callback) {
        workoutsApi.apiWorkoutsIdGet(id).enqueue(new Callback<DetailedWorkoutPlan>() {
            @Override
            public void onResponse(Call<DetailedWorkoutPlan> call, Response<DetailedWorkoutPlan> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to get workout plan: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<DetailedWorkoutPlan> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void deleteWorkoutPlan(String id, WorkoutsCallback<SuccessMessage> callback) {
        workoutsApi.apiWorkoutsIdDelete(id).enqueue(new Callback<SuccessMessage>() {
            @Override
            public void onResponse(Call<SuccessMessage> call, Response<SuccessMessage> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to delete workout plan: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<SuccessMessage> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void bulkUpdateWorkoutPlan(String id, BulkUpdateWorkoutPlan bulkUpdateWorkoutPlan, WorkoutsCallback<DetailedWorkoutPlan> callback) {
        workoutsApi.apiWorkoutsIdBulkPut(id, bulkUpdateWorkoutPlan).enqueue(new Callback<DetailedWorkoutPlan>() {
            @Override
            public void onResponse(Call<DetailedWorkoutPlan> call, Response<DetailedWorkoutPlan> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to bulk update workout plan: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<DetailedWorkoutPlan> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}
