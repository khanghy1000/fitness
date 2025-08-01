package com.example.fitness.data.network.retrofit

import com.example.fitness.data.network.model.generated.*
import com.squareup.moshi.Json
import retrofit2.http.*
import retrofit2.Call

interface UsersApi {
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
     * @return [Call]<[NutritionPlanAssignment]>
     */
    @GET("api/users/nutrition/{nutritionPlanId}/assign")
    fun apiUsersNutritionNutritionPlanIdAssignGet(@Path("nutritionPlanId") nutritionPlanId: kotlin.String, @Query("userId") userId: kotlin.String? = null): Call<NutritionPlanAssignment>

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
     * @return [Call]<[NutritionPlanAssignmentResponse]>
     */
    @POST("api/users/nutrition/{nutritionPlanId}/assign")
    fun apiUsersNutritionNutritionPlanIdAssignPost(@Path("nutritionPlanId") nutritionPlanId: kotlin.String, @Body assignNutritionPlan: AssignNutritionPlan? = null): Call<NutritionPlanAssignmentResponse>

    /**
     * GET api/users/nutrition-plans
     * Get user assigned nutrition plans
     * Get all nutrition plans assigned to the current user
     * Responses:
     *  - 200: List of assigned nutrition plans
     *  - 401: Unauthorized
     *
     * @return [Call]<[kotlin.collections.List<UserNutritionPlan>]>
     */
    @GET("api/users/nutrition-plans")
    fun apiUsersNutritionPlansGet(): Call<kotlin.collections.List<UserNutritionPlan>>

    /**
     * GET api/users/nutrition/user-plans/{userNutritionPlanId}/adherence
     * Get nutrition adherence history
     * Get adherence history for a user nutrition plan
     * Responses:
     *  - 200: Nutrition adherence history
     *  - 400: Missing userId parameter for coaches
     *  - 401: Unauthorized
     *  - 403: Access denied
     *
     * @param userNutritionPlanId User nutrition plan identifier
     * @param userId User ID for coach to specify which user (optional)
     * @return [Call]<[kotlin.collections.List<NutritionAdherenceHistory>]>
     */
    @GET("api/users/nutrition/user-plans/{userNutritionPlanId}/adherence")
    fun apiUsersNutritionUserPlansUserNutritionPlanIdAdherenceGet(@Path("userNutritionPlanId") userNutritionPlanId: kotlin.String, @Query("userId") userId: kotlin.String? = null): Call<kotlin.collections.List<NutritionAdherenceHistory>>

    /**
     * POST api/users/nutrition/user-plans/{userNutritionPlanId}/meals/{mealId}/complete
     * Complete a meal
     * Mark a meal as completed with actual consumption data. Automatically creates or updates nutrition adherence record.
     * Responses:
     *  - 201: Meal completed successfully
     *  - 400: Invalid input data
     *  - 401: Unauthorized
     *  - 404: User nutrition plan or meal not found
     *
     * @param userNutritionPlanId User nutrition plan identifier
     * @param mealId Nutrition plan meal identifier
     * @param mealCompletion Meal completion data (optional)
     * @return [Call]<[MealCompletionResponse]>
     */
    @POST("api/users/nutrition/user-plans/{userNutritionPlanId}/meals/{mealId}/complete")
    fun apiUsersNutritionUserPlansUserNutritionPlanIdMealsMealIdCompletePost(@Path("userNutritionPlanId") userNutritionPlanId: kotlin.String, @Path("mealId") mealId: kotlin.String, @Body mealCompletion: MealCompletion? = null): Call<MealCompletionResponse>


    /**
    * enum for parameter role
    */
    enum class RoleApiUsersSearchGet(val value: kotlin.String) {
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
     * @return [Call]<[kotlin.collections.List<User>]>
     */
    @GET("api/users/search")
    fun apiUsersSearchGet(@Query("query") query: kotlin.String, @Query("role") role: RoleApiUsersSearchGet? = null): Call<kotlin.collections.List<User>>

    /**
     * GET api/users/stats
     * Get user body stats
     * Get all body measurement statistics for the current user
     * Responses:
     *  - 200: User body stats history
     *  - 401: Unauthorized
     *
     * @return [Call]<[kotlin.collections.List<UserStats>]>
     */
    @GET("api/users/stats")
    fun apiUsersStatsGet(): Call<kotlin.collections.List<UserStats>>

    /**
     * GET api/users/stats/latest
     * Get latest user body stats
     * Get the most recent body measurement statistics for the current user
     * Responses:
     *  - 200: Latest user body stats
     *  - 401: Unauthorized
     *
     * @return [Call]<[LatestUserStats]>
     */
    @GET("api/users/stats/latest")
    fun apiUsersStatsLatestGet(): Call<LatestUserStats>

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
     * @return [Call]<[UserStatsResponse]>
     */
    @POST("api/users/stats")
    fun apiUsersStatsPost(@Body recordUserStats: RecordUserStats? = null): Call<UserStatsResponse>

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
     * @return [Call]<[ExerciseResult]>
     */
    @POST("api/users/workout/exercise-results")
    fun apiUsersWorkoutExerciseResultsPost(@Body recordExerciseResult: RecordExerciseResult? = null): Call<ExerciseResult>

    /**
     * GET api/users/workout-plans
     * Get user assigned workout plans
     * Get all workout plans assigned to the current user
     * Responses:
     *  - 200: List of assigned workout plans
     *  - 401: Unauthorized
     *
     * @return [Call]<[kotlin.collections.List<UserWorkoutPlan>]>
     */
    @GET("api/users/workout-plans")
    fun apiUsersWorkoutPlansGet(): Call<kotlin.collections.List<UserWorkoutPlan>>

    /**
     * GET api/users/workout/user-plans/{userWorkoutPlanId}/results
     * Get user workout plan results
     * Get exercise results for a specific user workout plan
     * Responses:
     *  - 200: Workout plan exercise results
     *  - 400: Missing userId parameter for coaches
     *  - 401: Unauthorized
     *  - 403: Access denied
     *  - 404: Workout plan not found or not assigned to user
     *
     * @param userWorkoutPlanId User workout plan identifier
     * @param userId User ID for coach to specify which user (optional)
     * @return [Call]<[WorkoutPlanResults]>
     */
    @GET("api/users/workout/user-plans/{userWorkoutPlanId}/results")
    fun apiUsersWorkoutUserPlansUserWorkoutPlanIdResultsGet(@Path("userWorkoutPlanId") userWorkoutPlanId: kotlin.String, @Query("userId") userId: kotlin.String? = null): Call<WorkoutPlanResults>

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
     * @return [Call]<[WorkoutPlanAssignment]>
     */
    @GET("api/users/workout/{workoutPlanId}/assign")
    fun apiUsersWorkoutWorkoutPlanIdAssignGet(@Path("workoutPlanId") workoutPlanId: kotlin.String, @Query("userId") userId: kotlin.String? = null): Call<WorkoutPlanAssignment>

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
     * @return [Call]<[WorkoutPlanAssignmentResponse]>
     */
    @POST("api/users/workout/{workoutPlanId}/assign")
    fun apiUsersWorkoutWorkoutPlanIdAssignPost(@Path("workoutPlanId") workoutPlanId: kotlin.String, @Body assignWorkoutPlan: AssignWorkoutPlan? = null): Call<WorkoutPlanAssignmentResponse>

}
