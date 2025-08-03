package com.example.fitness.data.network.retrofit

import com.example.fitness.data.network.model.generated.*
import com.squareup.moshi.Json
import retrofit2.http.*
import retrofit2.Call

interface WorkoutsApi {
    /**
     * GET api/workouts
     * Get all workout plans
     * Get all workout plans accessible to the current user
     * Responses:
     *  - 200: List of workout plans
     *  - 401: Unauthorized
     *
     * @return [Call]<[kotlin.collections.List<WorkoutPlan>]>
     */
    @GET("api/workouts")
    fun apiWorkoutsGet(): Call<kotlin.collections.List<WorkoutPlan>>

    /**
     * PUT api/workouts/{id}/bulk
     * Bulk update workout plan
     * Update a workout plan with all its days, exercises in one request. Items not included will be deleted.
     * Responses:
     *  - 200: Workout plan updated successfully
     *  - 400: Invalid input data
     *  - 401: Unauthorized
     *  - 404: Workout plan not found
     *  - 500: Internal server error
     *
     * @param id Unique identifier
     * @param bulkUpdateWorkoutPlan Complete workout plan data (optional)
     * @return [Call]<[DetailedWorkoutPlan]>
     */
    @PUT("api/workouts/{id}/bulk")
    fun apiWorkoutsIdBulkPut(@Path("id") id: kotlin.String, @Body bulkUpdateWorkoutPlan: BulkUpdateWorkoutPlan? = null): Call<DetailedWorkoutPlan>

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
     * @return [Call]<[SuccessMessage]>
     */
    @DELETE("api/workouts/{id}")
    fun apiWorkoutsIdDelete(@Path("id") id: kotlin.String): Call<SuccessMessage>

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
     * @return [Call]<[DetailedWorkoutPlan]>
     */
    @GET("api/workouts/{id}")
    fun apiWorkoutsIdGet(@Path("id") id: kotlin.String): Call<DetailedWorkoutPlan>

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
     * @return [Call]<[WorkoutPlan]>
     */
    @POST("api/workouts")
    fun apiWorkoutsPost(@Body createWorkoutPlan: CreateWorkoutPlan? = null): Call<WorkoutPlan>

}
