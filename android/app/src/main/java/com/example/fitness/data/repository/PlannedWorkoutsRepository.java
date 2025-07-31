package com.example.fitness.data.repository;

import com.example.fitness.data.network.retrofit.PlannedWorkoutsApi;
import com.example.fitness.data.network.model.generated.*;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class PlannedWorkoutsRepository {
    private final PlannedWorkoutsApi plannedWorkoutsApi;

    @Inject
    public PlannedWorkoutsRepository(PlannedWorkoutsApi plannedWorkoutsApi) {
        this.plannedWorkoutsApi = plannedWorkoutsApi;
    }

    public interface PlannedWorkoutsCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public void getUserPlannedWorkouts(PlannedWorkoutsCallback<java.util.List<PlannedWorkout>> callback) {
        plannedWorkoutsApi.apiPlannedWorkoutsGet().enqueue(new Callback<java.util.List<PlannedWorkout>>() {
            @Override
            public void onResponse(Call<java.util.List<PlannedWorkout>> call, Response<java.util.List<PlannedWorkout>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to get planned workouts: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<java.util.List<PlannedWorkout>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void createPlannedWorkout(CreatePlannedWorkout createPlannedWorkout, PlannedWorkoutsCallback<PlannedWorkout> callback) {
        plannedWorkoutsApi.apiPlannedWorkoutsPost(createPlannedWorkout).enqueue(new Callback<PlannedWorkout>() {
            @Override
            public void onResponse(Call<PlannedWorkout> call, Response<PlannedWorkout> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to create planned workout: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<PlannedWorkout> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getPlannedWorkoutById(String id, PlannedWorkoutsCallback<PlannedWorkout> callback) {
        plannedWorkoutsApi.apiPlannedWorkoutsIdGet(id).enqueue(new Callback<PlannedWorkout>() {
            @Override
            public void onResponse(Call<PlannedWorkout> call, Response<PlannedWorkout> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to get planned workout: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<PlannedWorkout> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void updatePlannedWorkout(String id, UpdatePlannedWorkout updatePlannedWorkout, PlannedWorkoutsCallback<PlannedWorkout> callback) {
        plannedWorkoutsApi.apiPlannedWorkoutsIdPut(id, updatePlannedWorkout).enqueue(new Callback<PlannedWorkout>() {
            @Override
            public void onResponse(Call<PlannedWorkout> call, Response<PlannedWorkout> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to update planned workout: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<PlannedWorkout> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void deletePlannedWorkout(String id, PlannedWorkoutsCallback<SuccessMessage> callback) {
        plannedWorkoutsApi.apiPlannedWorkoutsIdDelete(id).enqueue(new Callback<SuccessMessage>() {
            @Override
            public void onResponse(Call<SuccessMessage> call, Response<SuccessMessage> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to delete planned workout: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<SuccessMessage> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void togglePlannedWorkoutStatus(String id, TogglePlannedWorkout togglePlannedWorkout, PlannedWorkoutsCallback<PlannedWorkout> callback) {
        plannedWorkoutsApi.apiPlannedWorkoutsIdTogglePost(id, togglePlannedWorkout).enqueue(new Callback<PlannedWorkout>() {
            @Override
            public void onResponse(Call<PlannedWorkout> call, Response<PlannedWorkout> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to toggle planned workout status: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<PlannedWorkout> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getTodayPlannedWorkouts(PlannedWorkoutsCallback<java.util.List<PlannedWorkout>> callback) {
        plannedWorkoutsApi.apiPlannedWorkoutsTodayGet().enqueue(new Callback<java.util.List<PlannedWorkout>>() {
            @Override
            public void onResponse(Call<java.util.List<PlannedWorkout>> call, Response<java.util.List<PlannedWorkout>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to get today's planned workouts: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<java.util.List<PlannedWorkout>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getPlannedWorkoutsForWeekday(String weekday, PlannedWorkoutsCallback<java.util.List<PlannedWorkout>> callback) {
        plannedWorkoutsApi.apiPlannedWorkoutsWeekdayWeekdayGet(weekday).enqueue(new Callback<java.util.List<PlannedWorkout>>() {
            @Override
            public void onResponse(Call<java.util.List<PlannedWorkout>> call, Response<java.util.List<PlannedWorkout>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to get planned workouts for weekday: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<java.util.List<PlannedWorkout>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}
