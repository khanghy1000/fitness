package com.example.fitness.data.repository;

import com.example.fitness.R;
import com.example.fitness.data.network.retrofit.ExercisesApi;
import com.example.fitness.data.network.model.generated.*;
import com.example.fitness.model.ExerciseMetaData;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class ExercisesRepository {
    private final ExercisesApi exercisesApi;
    private List<ExerciseMetaData> exerciseMetaData;

    @Inject
    public ExercisesRepository(ExercisesApi exercisesApi) {
        this.exercisesApi = exercisesApi;

        this.exerciseMetaData = new ArrayList<>();

        exerciseMetaData.add(new ExerciseMetaData("Squat", R.drawable.exercise_squat, "squat"));
        exerciseMetaData.add(new ExerciseMetaData("Sit-up", R.drawable.exercise_squat, "sit_up"));
        exerciseMetaData.add(new ExerciseMetaData("Lunge", R.drawable.exercise_lunge, "lunge"));
//        exerciseMetaData.add(new ExerciseMetaData("Bicep Curl", R.drawable.exercise_bicep_curl, "bicep_curl"));
//        exerciseMetaData.add(new ExerciseMetaData("Crunch", R.drawable.exercise_crunch, "crunch"));
//        exerciseMetaData.add(new ExerciseMetaData("Heel Touch", R.drawable.exercise_heel_touch, "heel_touch"));
        exerciseMetaData.add(new ExerciseMetaData("Plank", R.drawable.exercise_plank, "plank"));
        exerciseMetaData.add(new ExerciseMetaData("Cobra", R.drawable.exercise_cobra, "cobra"));
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

    public int getExerciseImageResourceByName(String name) {
        for (ExerciseMetaData image : exerciseMetaData) {
            if (image.getName().equalsIgnoreCase(name) && image.getImageResourceId() != 0) {
                return image.getImageResourceId();
            }
        }
        return R.drawable.placeholder_exercise; // Default image if not found
    }

    public String getExerciseLabelByName(String name) {
        for (ExerciseMetaData image : exerciseMetaData) {
            if (image.getName().equalsIgnoreCase(name)) {
                return image.getLabel();
            }
        }
        return null;
    }

    public String getAllExerciseLabels() {
        StringBuilder labels = new StringBuilder();
        for (ExerciseMetaData metaData : exerciseMetaData) {
            if (labels.length() > 0) labels.append(", ");
            labels.append(metaData.getName()).append("->").append(metaData.getLabel());
        }
        return labels.toString();
    }
}
