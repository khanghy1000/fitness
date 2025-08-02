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

    public void getWorkoutPlanById(String id, WorkoutsCallback<WorkoutPlan> callback) {
        workoutsApi.apiWorkoutsIdGet(id).enqueue(new Callback<WorkoutPlan>() {
            @Override
            public void onResponse(Call<WorkoutPlan> call, Response<WorkoutPlan> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to get workout plan: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<WorkoutPlan> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void updateWorkoutPlan(String id, UpdateWorkoutPlan updateWorkoutPlan, WorkoutsCallback<WorkoutPlan> callback) {
        workoutsApi.apiWorkoutsIdPut(id, updateWorkoutPlan).enqueue(new Callback<WorkoutPlan>() {
            @Override
            public void onResponse(Call<WorkoutPlan> call, Response<WorkoutPlan> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to update workout plan: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<WorkoutPlan> call, Throwable t) {
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

    public void getWorkoutPlanDays(String id, WorkoutsCallback<java.util.List<WorkoutPlanDay>> callback) {
        workoutsApi.apiWorkoutsIdDaysGet(id).enqueue(new Callback<java.util.List<WorkoutPlanDay>>() {
            @Override
            public void onResponse(Call<java.util.List<WorkoutPlanDay>> call, Response<java.util.List<WorkoutPlanDay>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to get workout plan days: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<java.util.List<WorkoutPlanDay>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void createWorkoutPlanDay(String id, AddDayToWorkoutPlan addDayToWorkoutPlan, WorkoutsCallback<WorkoutPlanDay> callback) {
        workoutsApi.apiWorkoutsIdDaysPost(id, addDayToWorkoutPlan).enqueue(new Callback<WorkoutPlanDay>() {
            @Override
            public void onResponse(Call<WorkoutPlanDay> call, Response<WorkoutPlanDay> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to create workout plan day: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<WorkoutPlanDay> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getWorkoutPlanDay(String id, WorkoutsCallback<WorkoutPlanDay> callback) {
        workoutsApi.apiWorkoutsDaysIdGet(id).enqueue(new Callback<WorkoutPlanDay>() {
            @Override
            public void onResponse(Call<WorkoutPlanDay> call, Response<WorkoutPlanDay> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to get workout plan day: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<WorkoutPlanDay> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void updateWorkoutPlanDay(String id, UpdateWorkoutPlanDay updateWorkoutPlanDay, WorkoutsCallback<WorkoutPlanDay> callback) {
        workoutsApi.apiWorkoutsDaysIdPut(id, updateWorkoutPlanDay).enqueue(new Callback<WorkoutPlanDay>() {
            @Override
            public void onResponse(Call<WorkoutPlanDay> call, Response<WorkoutPlanDay> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to update workout plan day: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<WorkoutPlanDay> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void deleteWorkoutPlanDay(String id, WorkoutsCallback<SuccessMessage> callback) {
        workoutsApi.apiWorkoutsDaysIdDelete(id).enqueue(new Callback<SuccessMessage>() {
            @Override
            public void onResponse(Call<SuccessMessage> call, Response<SuccessMessage> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to delete workout plan day: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<SuccessMessage> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getWorkoutPlanDayExercises(String id, WorkoutsCallback<java.util.List<WorkoutPlanDayExercise>> callback) {
        workoutsApi.apiWorkoutsDaysIdExercisesGet(id).enqueue(new Callback<java.util.List<WorkoutPlanDayExercise>>() {
            @Override
            public void onResponse(Call<java.util.List<WorkoutPlanDayExercise>> call, Response<java.util.List<WorkoutPlanDayExercise>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to get workout plan day exercises: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<java.util.List<WorkoutPlanDayExercise>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void addExerciseToWorkoutPlanDay(String id, AddExerciseToPlanDay addExerciseToPlanDay, WorkoutsCallback<WorkoutPlanDayExercise> callback) {
        workoutsApi.apiWorkoutsDaysIdExercisesPost(id, addExerciseToPlanDay).enqueue(new Callback<WorkoutPlanDayExercise>() {
            @Override
            public void onResponse(Call<WorkoutPlanDayExercise> call, Response<WorkoutPlanDayExercise> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to add exercise to workout plan day: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<WorkoutPlanDayExercise> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getWorkoutPlanExercise(String id, WorkoutsCallback<WorkoutPlanDayExercise> callback) {
        workoutsApi.apiWorkoutsExercisesIdGet(id).enqueue(new Callback<WorkoutPlanDayExercise>() {
            @Override
            public void onResponse(Call<WorkoutPlanDayExercise> call, Response<WorkoutPlanDayExercise> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to get workout plan exercise: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<WorkoutPlanDayExercise> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void updateWorkoutPlanExercise(String id, UpdateExerciseInPlanDay updateExerciseInPlanDay, WorkoutsCallback<WorkoutPlanDayExercise> callback) {
        workoutsApi.apiWorkoutsExercisesIdPut(id, updateExerciseInPlanDay).enqueue(new Callback<WorkoutPlanDayExercise>() {
            @Override
            public void onResponse(Call<WorkoutPlanDayExercise> call, Response<WorkoutPlanDayExercise> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to update workout plan exercise: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<WorkoutPlanDayExercise> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void deleteWorkoutPlanExercise(String id, WorkoutsCallback<SuccessMessage> callback) {
        workoutsApi.apiWorkoutsExercisesIdDelete(id).enqueue(new Callback<SuccessMessage>() {
            @Override
            public void onResponse(Call<SuccessMessage> call, Response<SuccessMessage> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to delete workout plan exercise: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<SuccessMessage> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}
