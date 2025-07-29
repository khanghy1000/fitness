package com.example.fitness.data.network.retrofit

import retrofit2.http.*
import retrofit2.Response

import com.example.fitness.data.network.model.generated.AddDayToWorkoutPlan
import com.example.fitness.data.network.model.generated.AddExerciseToPlanDay
import com.example.fitness.data.network.model.generated.ApiWorkoutsDaysIdExercisesPost201Response
import com.example.fitness.data.network.model.generated.CreateWorkoutPlan
import com.example.fitness.data.network.model.generated.SuccessMessage
import com.example.fitness.data.network.model.generated.UpdateExerciseInPlanDay
import com.example.fitness.data.network.model.generated.UpdateWorkoutPlan
import com.example.fitness.data.network.model.generated.UpdateWorkoutPlanDay
import com.example.fitness.data.network.model.generated.WorkoutPlan
import com.example.fitness.data.network.model.generated.WorkoutPlanDay
import com.example.fitness.data.network.model.generated.WorkoutPlanDayExercise

interface WorkoutsApi {
    /**
     * DELETE api/workouts/days/{id}
     * Delete workout plan day
     * Delete a workout plan day
     * Responses:
     *  - 200: Workout plan day deleted successfully
     *  - 401: Unauthorized
     *  - 404: Workout plan day not found
     *
     * @param id Unique identifier
     * @return [SuccessMessage]
     */
    @DELETE("api/workouts/days/{id}")
    suspend fun apiWorkoutsDaysIdDelete(@Path("id") id: kotlin.String): Response<SuccessMessage>

    /**
     * GET api/workouts/days/{id}/exercises
     * Get workout plan day exercises
     * Get all exercises for a workout plan day
     * Responses:
     *  - 200: List of workout plan exercises
     *  - 401: Unauthorized
     *  - 404: Workout plan day not found
     *
     * @param id Unique identifier
     * @return [kotlin.collections.List<WorkoutPlanDayExercise>]
     */
    @GET("api/workouts/days/{id}/exercises")
    suspend fun apiWorkoutsDaysIdExercisesGet(@Path("id") id: kotlin.String): Response<kotlin.collections.List<WorkoutPlanDayExercise>>

    /**
     * POST api/workouts/days/{id}/exercises
     * Add exercise to workout plan day
     * Add a new exercise to a workout plan day
     * Responses:
     *  - 201: Exercise added to workout plan day successfully
     *  - 400: Invalid input data
     *  - 401: Unauthorized
     *
     * @param id Unique identifier
     * @param addExerciseToPlanDay Exercise data (optional)
     * @return [ApiWorkoutsDaysIdExercisesPost201Response]
     */
    @POST("api/workouts/days/{id}/exercises")
    suspend fun apiWorkoutsDaysIdExercisesPost(@Path("id") id: kotlin.String, @Body addExerciseToPlanDay: AddExerciseToPlanDay? = null): Response<ApiWorkoutsDaysIdExercisesPost201Response>

    /**
     * GET api/workouts/days/{id}
     * Get workout plan day
     * Get a specific workout plan day by ID
     * Responses:
     *  - 200: Workout plan day details
     *  - 401: Unauthorized
     *  - 404: Workout plan day not found
     *
     * @param id Unique identifier
     * @return [WorkoutPlanDay]
     */
    @GET("api/workouts/days/{id}")
    suspend fun apiWorkoutsDaysIdGet(@Path("id") id: kotlin.String): Response<WorkoutPlanDay>

    /**
     * PUT api/workouts/days/{id}
     * Update workout plan day
     * Update a workout plan day
     * Responses:
     *  - 200: Workout plan day updated successfully
     *  - 400: Invalid input data
     *  - 401: Unauthorized
     *  - 404: Workout plan day not found
     *
     * @param id Unique identifier
     * @param updateWorkoutPlanDay Updated workout plan day data (optional)
     * @return [WorkoutPlanDay]
     */
    @PUT("api/workouts/days/{id}")
    suspend fun apiWorkoutsDaysIdPut(@Path("id") id: kotlin.String, @Body updateWorkoutPlanDay: UpdateWorkoutPlanDay? = null): Response<WorkoutPlanDay>

    /**
     * DELETE api/workouts/exercises/{id}
     * Delete workout plan exercise
     * Delete a workout plan exercise
     * Responses:
     *  - 200: Workout plan exercise deleted successfully
     *  - 401: Unauthorized
     *  - 404: Workout plan exercise not found
     *
     * @param id Unique identifier
     * @return [SuccessMessage]
     */
    @DELETE("api/workouts/exercises/{id}")
    suspend fun apiWorkoutsExercisesIdDelete(@Path("id") id: kotlin.String): Response<SuccessMessage>

    /**
     * GET api/workouts/exercises/{id}
     * Get workout plan exercise
     * Get a specific workout plan exercise by ID
     * Responses:
     *  - 200: Workout plan exercise details
     *  - 401: Unauthorized
     *  - 404: Workout plan exercise not found
     *
     * @param id Unique identifier
     * @return [WorkoutPlanDayExercise]
     */
    @GET("api/workouts/exercises/{id}")
    suspend fun apiWorkoutsExercisesIdGet(@Path("id") id: kotlin.String): Response<WorkoutPlanDayExercise>

    /**
     * PUT api/workouts/exercises/{id}
     * Update workout plan exercise
     * Update a workout plan exercise
     * Responses:
     *  - 200: Workout plan exercise updated successfully
     *  - 400: Invalid input data
     *  - 401: Unauthorized
     *  - 404: Workout plan exercise not found
     *
     * @param id Unique identifier
     * @param updateExerciseInPlanDay Updated exercise data (optional)
     * @return [ApiWorkoutsDaysIdExercisesPost201Response]
     */
    @PUT("api/workouts/exercises/{id}")
    suspend fun apiWorkoutsExercisesIdPut(@Path("id") id: kotlin.String, @Body updateExerciseInPlanDay: UpdateExerciseInPlanDay? = null): Response<ApiWorkoutsDaysIdExercisesPost201Response>

    /**
     * GET api/workouts
     * Get all workout plans
     * Get all workout plans accessible to the current user
     * Responses:
     *  - 200: List of workout plans
     *  - 401: Unauthorized
     *
     * @return [kotlin.collections.List<WorkoutPlan>]
     */
    @GET("api/workouts")
    suspend fun apiWorkoutsGet(): Response<kotlin.collections.List<WorkoutPlan>>

    /**
     * GET api/workouts/{id}/days
     * Get workout plan days
     * Get all days for a workout plan
     * Responses:
     *  - 200: List of workout plan days
     *  - 401: Unauthorized
     *  - 404: Workout plan not found
     *
     * @param id Unique identifier
     * @return [kotlin.collections.List<WorkoutPlanDay>]
     */
    @GET("api/workouts/{id}/days")
    suspend fun apiWorkoutsIdDaysGet(@Path("id") id: kotlin.String): Response<kotlin.collections.List<WorkoutPlanDay>>

    /**
     * POST api/workouts/{id}/days
     * Create workout plan day
     * Create a new day for a workout plan
     * Responses:
     *  - 201: Workout plan day created successfully
     *  - 400: Invalid input data
     *  - 401: Unauthorized
     *
     * @param id Unique identifier
     * @param addDayToWorkoutPlan Workout plan day data (optional)
     * @return [WorkoutPlanDay]
     */
    @POST("api/workouts/{id}/days")
    suspend fun apiWorkoutsIdDaysPost(@Path("id") id: kotlin.String, @Body addDayToWorkoutPlan: AddDayToWorkoutPlan? = null): Response<WorkoutPlanDay>

    /**
     * DELETE api/workouts/{id}
     * Delete workout plan
     * Delete a workout plan (coach only)
     * Responses:
     *  - 200: Workout plan deleted successfully
     *  - 401: Unauthorized - Coach role required
     *  - 404: Workout plan not found
     *
     * @param id Unique identifier
     * @return [SuccessMessage]
     */
    @DELETE("api/workouts/{id}")
    suspend fun apiWorkoutsIdDelete(@Path("id") id: kotlin.String): Response<SuccessMessage>

    /**
     * GET api/workouts/{id}
     * Get workout plan by ID
     * Get a specific workout plan with full details
     * Responses:
     *  - 200: Workout plan details
     *  - 401: Unauthorized
     *  - 404: Workout plan not found
     *
     * @param id Unique identifier
     * @return [WorkoutPlan]
     */
    @GET("api/workouts/{id}")
    suspend fun apiWorkoutsIdGet(@Path("id") id: kotlin.String): Response<WorkoutPlan>

    /**
     * PUT api/workouts/{id}
     * Update workout plan
     * Update a workout plan (coach only)
     * Responses:
     *  - 200: Workout plan updated successfully
     *  - 400: Invalid input data
     *  - 401: Unauthorized - Coach role required
     *  - 404: Workout plan not found
     *
     * @param id Unique identifier
     * @param updateWorkoutPlan Updated workout plan data (optional)
     * @return [WorkoutPlan]
     */
    @PUT("api/workouts/{id}")
    suspend fun apiWorkoutsIdPut(@Path("id") id: kotlin.String, @Body updateWorkoutPlan: UpdateWorkoutPlan? = null): Response<WorkoutPlan>

    /**
     * POST api/workouts
     * Create workout plan
     * Create a new workout plan
     * Responses:
     *  - 201: Workout plan created successfully
     *  - 400: Invalid input data
     *  - 401: Unauthorized
     *
     * @param createWorkoutPlan Workout plan data (optional)
     * @return [WorkoutPlan]
     */
    @POST("api/workouts")
    suspend fun apiWorkoutsPost(@Body createWorkoutPlan: CreateWorkoutPlan? = null): Response<WorkoutPlan>

}
