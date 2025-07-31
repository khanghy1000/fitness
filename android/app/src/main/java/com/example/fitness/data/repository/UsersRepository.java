package com.example.fitness.data.repository;

import com.example.fitness.data.network.retrofit.UsersApi;
import com.example.fitness.data.network.model.generated.*;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class UsersRepository {
    private final UsersApi usersApi;

    @Inject
    public UsersRepository(UsersApi usersApi) {
        this.usersApi = usersApi;
    }

    public interface UsersCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public void searchUsers(String query, UsersApi.RoleApiUsersSearchGet role, UsersCallback<java.util.List<User>> callback) {
        usersApi.apiUsersSearchGet(query, role).enqueue(new Callback<java.util.List<User>>() {
            @Override
            public void onResponse(Call<java.util.List<User>> call, Response<java.util.List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to search users: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<java.util.List<User>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getUserStats(UsersCallback<java.util.List<UserStats>> callback) {
        usersApi.apiUsersStatsGet().enqueue(new Callback<java.util.List<UserStats>>() {
            @Override
            public void onResponse(Call<java.util.List<UserStats>> call, Response<java.util.List<UserStats>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to get user stats: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<java.util.List<UserStats>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getLatestUserStats(UsersCallback<LatestUserStats> callback) {
        usersApi.apiUsersStatsLatestGet().enqueue(new Callback<LatestUserStats>() {
            @Override
            public void onResponse(Call<LatestUserStats> call, Response<LatestUserStats> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to get latest user stats: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<LatestUserStats> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void recordUserStats(RecordUserStats recordUserStats, UsersCallback<UserStatsResponse> callback) {
        usersApi.apiUsersStatsPost(recordUserStats).enqueue(new Callback<UserStatsResponse>() {
            @Override
            public void onResponse(Call<UserStatsResponse> call, Response<UserStatsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to record user stats: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<UserStatsResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void recordExerciseResult(RecordExerciseResult recordExerciseResult, UsersCallback<ExerciseResult> callback) {
        usersApi.apiUsersWorkoutExerciseResultsPost(recordExerciseResult).enqueue(new Callback<ExerciseResult>() {
            @Override
            public void onResponse(Call<ExerciseResult> call, Response<ExerciseResult> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to record exercise result: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ExerciseResult> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getUserWorkoutPlans(UsersCallback<java.util.List<UserWorkoutPlan>> callback) {
        usersApi.apiUsersWorkoutPlansGet().enqueue(new Callback<java.util.List<UserWorkoutPlan>>() {
            @Override
            public void onResponse(Call<java.util.List<UserWorkoutPlan>> call, Response<java.util.List<UserWorkoutPlan>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to get user workout plans: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<java.util.List<UserWorkoutPlan>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getWorkoutPlanAssignment(String workoutPlanId, String userId, UsersCallback<WorkoutPlanAssignment> callback) {
        usersApi.apiUsersWorkoutWorkoutPlanIdAssignGet(workoutPlanId, userId).enqueue(new Callback<WorkoutPlanAssignment>() {
            @Override
            public void onResponse(Call<WorkoutPlanAssignment> call, Response<WorkoutPlanAssignment> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to get workout plan assignment: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<WorkoutPlanAssignment> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void assignWorkoutPlan(String workoutPlanId, AssignWorkoutPlan assignWorkoutPlan, UsersCallback<WorkoutPlanAssignmentResponse> callback) {
        usersApi.apiUsersWorkoutWorkoutPlanIdAssignPost(workoutPlanId, assignWorkoutPlan).enqueue(new Callback<WorkoutPlanAssignmentResponse>() {
            @Override
            public void onResponse(Call<WorkoutPlanAssignmentResponse> call, Response<WorkoutPlanAssignmentResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to assign workout plan: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<WorkoutPlanAssignmentResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getWorkoutPlanResults(String workoutPlanId, String userId, UsersCallback<WorkoutPlanResults> callback) {
        usersApi.apiUsersWorkoutWorkoutPlanIdResultsGet(workoutPlanId, userId).enqueue(new Callback<WorkoutPlanResults>() {
            @Override
            public void onResponse(Call<WorkoutPlanResults> call, Response<WorkoutPlanResults> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to get workout plan results: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<WorkoutPlanResults> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getUserNutritionPlans(UsersCallback<java.util.List<UserNutritionPlan>> callback) {
        usersApi.apiUsersNutritionPlansGet().enqueue(new Callback<java.util.List<UserNutritionPlan>>() {
            @Override
            public void onResponse(Call<java.util.List<UserNutritionPlan>> call, Response<java.util.List<UserNutritionPlan>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to get user nutrition plans: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<java.util.List<UserNutritionPlan>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getNutritionPlanAssignment(String nutritionPlanId, String userId, UsersCallback<NutritionPlanAssignment> callback) {
        usersApi.apiUsersNutritionNutritionPlanIdAssignGet(nutritionPlanId, userId).enqueue(new Callback<NutritionPlanAssignment>() {
            @Override
            public void onResponse(Call<NutritionPlanAssignment> call, Response<NutritionPlanAssignment> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to get nutrition plan assignment: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<NutritionPlanAssignment> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void assignNutritionPlan(String nutritionPlanId, AssignNutritionPlan assignNutritionPlan, UsersCallback<NutritionPlanAssignmentResponse> callback) {
        usersApi.apiUsersNutritionNutritionPlanIdAssignPost(nutritionPlanId, assignNutritionPlan).enqueue(new Callback<NutritionPlanAssignmentResponse>() {
            @Override
            public void onResponse(Call<NutritionPlanAssignmentResponse> call, Response<NutritionPlanAssignmentResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to assign nutrition plan: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<NutritionPlanAssignmentResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Additional nutrition adherence methods would continue here...
    // For brevity, including the main ones
}
