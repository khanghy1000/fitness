package com.example.fitness.data.repository;

import com.example.fitness.data.network.retrofit.ExercisesApi;
import com.example.fitness.data.network.model.generated.*;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class ExercisesRepository {
    private final ExercisesApi exercisesApi;

    @Inject
    public ExercisesRepository(ExercisesApi exercisesApi) {
        this.exercisesApi = exercisesApi;
    }

    public interface ExercisesCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public void getAllExerciseTypes(ExercisesCallback<java.util.List<ExerciseType>> callback) {
        exercisesApi.apiExercisesGet().enqueue(new Callback<java.util.List<ExerciseType>>() {
            @Override
            public void onResponse(Call<java.util.List<ExerciseType>> call, Response<java.util.List<ExerciseType>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to get exercise types: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<java.util.List<ExerciseType>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getExerciseTypeById(String id, ExercisesCallback<ExerciseType> callback) {
        exercisesApi.apiExercisesIdGet(id).enqueue(new Callback<ExerciseType>() {
            @Override
            public void onResponse(Call<ExerciseType> call, Response<ExerciseType> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to get exercise type: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ExerciseType> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getExerciseTypeByName(String name, ExercisesCallback<ExerciseType> callback) {
        exercisesApi.apiExercisesNameNameGet(name).enqueue(new Callback<ExerciseType>() {
            @Override
            public void onResponse(Call<ExerciseType> call, Response<ExerciseType> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to get exercise type by name: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ExerciseType> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}
