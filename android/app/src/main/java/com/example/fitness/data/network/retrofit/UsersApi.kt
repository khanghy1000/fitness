package com.example.fitness.data.network.retrofit

import retrofit2.http.*
import retrofit2.Response
import com.squareup.moshi.Json

import com.example.fitness.data.network.model.generated.AssignNutritionPlan
import com.example.fitness.data.network.model.generated.AssignWorkoutPlan
import com.example.fitness.data.network.model.generated.ExerciseResult
import com.example.fitness.data.network.model.generated.LatestUserStats
import com.example.fitness.data.network.model.generated.MealCompletion
import com.example.fitness.data.network.model.generated.MealCompletionResponse
import com.example.fitness.data.network.model.generated.NutritionAdherence
import com.example.fitness.data.network.model.generated.NutritionAdherenceHistory
import com.example.fitness.data.network.model.generated.NutritionAdherenceResponse
import com.example.fitness.data.network.model.generated.NutritionPlanAssignment
import com.example.fitness.data.network.model.generated.NutritionPlanAssignmentResponse
import com.example.fitness.data.network.model.generated.RecordExerciseResult
import com.example.fitness.data.network.model.generated.RecordUserStats
import com.example.fitness.data.network.model.generated.UpdatedNutritionAdherenceResponse
import com.example.fitness.data.network.model.generated.User
import com.example.fitness.data.network.model.generated.UserNutritionPlan
import com.example.fitness.data.network.model.generated.UserStats
import com.example.fitness.data.network.model.generated.UserStatsResponse
import com.example.fitness.data.network.model.generated.UserWorkoutPlan
import com.example.fitness.data.network.model.generated.WorkoutPlanAssignment
import com.example.fitness.data.network.model.generated.WorkoutPlanAssignmentResponse
import com.example.fitness.data.network.model.generated.WorkoutPlanResults

interface UsersApi {
    /**
     * POST api/users/nutrition/{nutritionPlanId}/adherence/{adherenceId}/meals/{mealId}/complete
     * Complete a meal
     * Mark a meal as completed with actual consumption data
     * Responses:
     *  - 201: Meal completed successfully
     *  - 400: Invalid input data
     *  - 401: Unauthorized
     *
     * @param nutritionPlanId 
     * @param adherenceId 
     * @param mealId 
     * @param mealCompletion Meal completion data (optional)
     * @return [MealCompletionResponse]
     */
    @POST("api/users/nutrition/{nutritionPlanId}/adherence/{adherenceId}/meals/{mealId}/complete")
    suspend fun apiUsersNutritionNutritionPlanIdAdherenceAdherenceIdMealsMealIdCompletePost(@Path("nutritionPlanId") nutritionPlanId: String, @Path("adherenceId") adherenceId: String, @Path("mealId") mealId: String, @Body mealCompletion: MealCompletion? = null): Response<MealCompletionResponse>

    /**
     * GET api/users/nutrition/{nutritionPlanId}/adherence
     * Get nutrition adherence history
     * Get adherence history for a nutrition plan
     * Responses:
     *  - 200: Nutrition adherence history
     *  - 400: Missing userId parameter for coaches
     *  - 401: Unauthorized
     *  - 403: Access denied
     *
     * @param nutritionPlanId Nutrition plan identifier
     * @param userId User ID for coach to specify which user (optional)
     * @return [kotlin.collections.List<NutritionAdherenceHistory>]
     */
    @GET("api/users/nutrition/{nutritionPlanId}/adherence")
    suspend fun apiUsersNutritionNutritionPlanIdAdherenceGet(@Path("nutritionPlanId") nutritionPlanId: String, @Query("userId") userId: String? = null): Response<List<NutritionAdherenceHistory>>

    /**
     * PUT api/users/nutrition/{nutritionPlanId}/adherence/{id}
     * Update daily adherence record
     * Update an existing daily nutrition adherence record
     * Responses:
     *  - 200: Daily adherence record updated successfully
     *  - 400: Invalid input data
     *  - 401: Unauthorized
     *  - 404: Adherence record not found
     *
     * @param nutritionPlanId Nutrition plan identifier
     * @param id Unique identifier
     * @param nutritionAdherence Updated adherence data (optional)
     * @return [UpdatedNutritionAdherenceResponse]
     */
    @PUT("api/users/nutrition/{nutritionPlanId}/adherence/{id}")
    suspend fun apiUsersNutritionNutritionPlanIdAdherenceIdPut(@Path("nutritionPlanId") nutritionPlanId: String, @Path("id") id: String, @Body nutritionAdherence: NutritionAdherence? = null): Response<UpdatedNutritionAdherenceResponse>

    /**
     * POST api/users/nutrition/{nutritionPlanId}/adherence
     * Create daily adherence record
     * Create a daily nutrition adherence record
     * Responses:
     *  - 201: Daily adherence record created successfully
     *  - 400: Invalid input data
     *  - 401: Unauthorized
     *
     * @param nutritionPlanId Nutrition plan identifier
     * @param nutritionAdherence Adherence data (optional)
     * @return [NutritionAdherenceResponse]
     */
    @POST("api/users/nutrition/{nutritionPlanId}/adherence")
    suspend fun apiUsersNutritionNutritionPlanIdAdherencePost(@Path("nutritionPlanId") nutritionPlanId: String, @Body nutritionAdherence: NutritionAdherence? = null): Response<NutritionAdherenceResponse>

    /**
     * GET api/users/nutrition/{nutritionPlanId}/assign
     * Get nutrition plan assignment
     * Get nutrition plan assignment details for a specific plan
     * Responses:
     *  - 200: Nutrition plan assignment details
     *  - 400: Missing userId parameter for coaches
     *  - 401: Unauthorized
     *  - 403: Access denied
     *
     * @param nutritionPlanId Nutrition plan identifier
     * @param userId User ID for coach to specify which user (optional)
     * @return [NutritionPlanAssignment]
     */
    @GET("api/users/nutrition/{nutritionPlanId}/assign")
    suspend fun apiUsersNutritionNutritionPlanIdAssignGet(@Path("nutritionPlanId") nutritionPlanId: String, @Query("userId") userId: String? = null): Response<NutritionPlanAssignment>

    /**
     * POST api/users/nutrition/{nutritionPlanId}/assign
     * Assign nutrition plan
     * Assign a nutrition plan to a trainee (coach only)
     * Responses:
     *  - 201: Nutrition plan assigned successfully
     *  - 400: Invalid input data
     *  - 401: Unauthorized - Coach role required
     *
     * @param nutritionPlanId Nutrition plan identifier
     * @param assignNutritionPlan Assignment data (optional)
     * @return [NutritionPlanAssignmentResponse]
     */
    @POST("api/users/nutrition/{nutritionPlanId}/assign")
    suspend fun apiUsersNutritionNutritionPlanIdAssignPost(@Path("nutritionPlanId") nutritionPlanId: String, @Body assignNutritionPlan: AssignNutritionPlan? = null): Response<NutritionPlanAssignmentResponse>

    /**
     * GET api/users/nutrition-plans
     * Get user assigned nutrition plans
     * Get all nutrition plans assigned to the current user
     * Responses:
     *  - 200: List of assigned nutrition plans
     *  - 401: Unauthorized
     *
     * @return [kotlin.collections.List<UserNutritionPlan>]
     */
    @GET("api/users/nutrition-plans")
    suspend fun apiUsersNutritionPlansGet(): Response<List<UserNutritionPlan>>


    /**
    * enum for parameter role
    */
    enum class RoleApiUsersSearchGet(val value: String) {
        @Json(name = "coach") coach("coach"),
        @Json(name = "trainee") trainee("trainee")
    }

    /**
     * GET api/users/search
     * Search users
     * Search for users by name or email, optionally filtered by role
     * Responses:
     *  - 200: List of matching users
     *  - 400: Invalid query parameters
     *  - 401: Unauthorized
     *
     * @param query Search term for finding users
     * @param role Filter by user role (optional)
     * @return [kotlin.collections.List<User>]
     */
    @GET("api/users/search")
    suspend fun apiUsersSearchGet(@Query("query") query: String, @Query("role") role: RoleApiUsersSearchGet? = null): Response<List<User>>

    /**
     * GET api/users/stats
     * Get user body stats
     * Get all body measurement statistics for the current user
     * Responses:
     *  - 200: User body stats history
     *  - 401: Unauthorized
     *
     * @return [kotlin.collections.List<UserStats>]
     */
    @GET("api/users/stats")
    suspend fun apiUsersStatsGet(): Response<List<UserStats>>

    /**
     * GET api/users/stats/latest
     * Get latest user body stats
     * Get the most recent body measurement statistics for the current user
     * Responses:
     *  - 200: Latest user body stats
     *  - 401: Unauthorized
     *
     * @return [LatestUserStats]
     */
    @GET("api/users/stats/latest")
    suspend fun apiUsersStatsLatestGet(): Response<LatestUserStats>

    /**
     * POST api/users/stats
     * Record user body stats
     * Record new body measurements for the current user
     * Responses:
     *  - 201: Body stats recorded successfully
     *  - 400: Invalid input data
     *  - 401: Unauthorized
     *
     * @param recordUserStats Body measurement data (optional)
     * @return [UserStatsResponse]
     */
    @POST("api/users/stats")
    suspend fun apiUsersStatsPost(@Body recordUserStats: RecordUserStats? = null): Response<UserStatsResponse>

    /**
     * POST api/users/workout/exercise-results
     * Record exercise result
     * Record the result of an exercise performance
     * Responses:
     *  - 201: Exercise result recorded successfully
     *  - 400: Invalid input data
     *  - 401: Unauthorized
     *
     * @param recordExerciseResult Exercise result data (optional)
     * @return [ExerciseResult]
     */
    @POST("api/users/workout/exercise-results")
    suspend fun apiUsersWorkoutExerciseResultsPost(@Body recordExerciseResult: RecordExerciseResult? = null): Response<ExerciseResult>

    /**
     * GET api/users/workout-plans
     * Get user assigned workout plans
     * Get all workout plans assigned to the current user
     * Responses:
     *  - 200: List of assigned workout plans
     *  - 401: Unauthorized
     *
     * @return [kotlin.collections.List<UserWorkoutPlan>]
     */
    @GET("api/users/workout-plans")
    suspend fun apiUsersWorkoutPlansGet(): Response<List<UserWorkoutPlan>>

    /**
     * GET api/users/workout/{workoutPlanId}/assign
     * Get workout plan assignment
     * Get workout plan assignment details for a specific plan
     * Responses:
     *  - 200: Workout plan assignment details
     *  - 400: Missing userId parameter for coaches
     *  - 401: Unauthorized
     *  - 403: Access denied
     *
     * @param workoutPlanId Workout plan identifier
     * @param userId User ID for coach to specify which user (optional)
     * @return [WorkoutPlanAssignment]
     */
    @GET("api/users/workout/{workoutPlanId}/assign")
    suspend fun apiUsersWorkoutWorkoutPlanIdAssignGet(@Path("workoutPlanId") workoutPlanId: String, @Query("userId") userId: String? = null): Response<WorkoutPlanAssignment>

    /**
     * POST api/users/workout/{workoutPlanId}/assign
     * Assign workout plan
     * Assign a workout plan to a trainee (coach only)
     * Responses:
     *  - 201: Workout plan assigned successfully
     *  - 400: Invalid input data
     *  - 401: Unauthorized - Coach role required
     *
     * @param workoutPlanId Workout plan identifier
     * @param assignWorkoutPlan Assignment data (optional)
     * @return [WorkoutPlanAssignmentResponse]
     */
    @POST("api/users/workout/{workoutPlanId}/assign")
    suspend fun apiUsersWorkoutWorkoutPlanIdAssignPost(@Path("workoutPlanId") workoutPlanId: String, @Body assignWorkoutPlan: AssignWorkoutPlan? = null): Response<WorkoutPlanAssignmentResponse>

    /**
     * GET api/users/workout/{workoutPlanId}/results
     * Get workout plan results
     * Get exercise results for a specific workout plan
     * Responses:
     *  - 200: Workout plan exercise results
     *  - 400: Missing userId parameter for coaches
     *  - 401: Unauthorized
     *  - 403: Access denied
     *  - 404: Workout plan not found or not assigned to user
     *
     * @param workoutPlanId Workout plan identifier
     * @param userId User ID for coach to specify which user (optional)
     * @return [WorkoutPlanResults]
     */
    @GET("api/users/workout/{workoutPlanId}/results")
    suspend fun apiUsersWorkoutWorkoutPlanIdResultsGet(@Path("workoutPlanId") workoutPlanId: String, @Query("userId") userId: String? = null): Response<WorkoutPlanResults>

}
