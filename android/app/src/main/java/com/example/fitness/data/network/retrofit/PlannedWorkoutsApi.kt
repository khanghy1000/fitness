package com.example.fitness.data.network.retrofit

import retrofit2.http.*
import retrofit2.Response
import com.squareup.moshi.Json

import com.example.fitness.data.network.model.generated.CreatePlannedWorkout
import com.example.fitness.data.network.model.generated.PlannedWorkout
import com.example.fitness.data.network.model.generated.SuccessMessage
import com.example.fitness.data.network.model.generated.TogglePlannedWorkout
import com.example.fitness.data.network.model.generated.UpdatePlannedWorkout

interface PlannedWorkoutsApi {
    /**
     * GET api/planned-workouts
     * Get user planned workouts
     * Get all planned workouts for the current user
     * Responses:
     *  - 200: List of planned workouts
     *  - 401: Unauthorized
     *
     * @return [kotlin.collections.List<PlannedWorkout>]
     */
    @GET("api/planned-workouts")
    suspend fun apiPlannedWorkoutsGet(): Response<List<PlannedWorkout>>

    /**
     * DELETE api/planned-workouts/{id}
     * Delete planned workout
     * Delete a planned workout
     * Responses:
     *  - 200: Planned workout deleted successfully
     *  - 401: Unauthorized
     *  - 403: Access denied
     *  - 404: Planned workout not found
     *
     * @param id Unique identifier
     * @return [SuccessMessage]
     */
    @DELETE("api/planned-workouts/{id}")
    suspend fun apiPlannedWorkoutsIdDelete(@Path("id") id: String): Response<SuccessMessage>

    /**
     * GET api/planned-workouts/{id}
     * Get planned workout by ID
     * Get a specific planned workout by ID
     * Responses:
     *  - 200: Planned workout details
     *  - 401: Unauthorized
     *  - 403: Access denied
     *  - 404: Planned workout not found
     *
     * @param id Unique identifier
     * @return [PlannedWorkout]
     */
    @GET("api/planned-workouts/{id}")
    suspend fun apiPlannedWorkoutsIdGet(@Path("id") id: String): Response<PlannedWorkout>

    /**
     * PUT api/planned-workouts/{id}
     * Update planned workout
     * Update a planned workout
     * Responses:
     *  - 200: Planned workout updated successfully
     *  - 400: Invalid input data
     *  - 401: Unauthorized
     *  - 403: Access denied
     *  - 404: Planned workout not found
     *
     * @param id Unique identifier
     * @param updatePlannedWorkout Updated planned workout data (optional)
     * @return [PlannedWorkout]
     */
    @PUT("api/planned-workouts/{id}")
    suspend fun apiPlannedWorkoutsIdPut(@Path("id") id: String, @Body updatePlannedWorkout: UpdatePlannedWorkout? = null): Response<PlannedWorkout>

    /**
     * POST api/planned-workouts/{id}/toggle
     * Toggle planned workout status
     * Toggle the active status of a planned workout
     * Responses:
     *  - 200: Planned workout status toggled successfully
     *  - 400: Invalid input data
     *  - 401: Unauthorized
     *  - 403: Access denied
     *  - 404: Planned workout not found
     *
     * @param id Unique identifier
     * @param togglePlannedWorkout Toggle status data (optional)
     * @return [PlannedWorkout]
     */
    @POST("api/planned-workouts/{id}/toggle")
    suspend fun apiPlannedWorkoutsIdTogglePost(@Path("id") id: String, @Body togglePlannedWorkout: TogglePlannedWorkout? = null): Response<PlannedWorkout>

    /**
     * POST api/planned-workouts
     * Create planned workout
     * Create/Schedule a new planned workout
     * Responses:
     *  - 201: Planned workout created successfully
     *  - 400: Invalid input data
     *  - 401: Unauthorized
     *
     * @param createPlannedWorkout Planned workout data (optional)
     * @return [PlannedWorkout]
     */
    @POST("api/planned-workouts")
    suspend fun apiPlannedWorkoutsPost(@Body createPlannedWorkout: CreatePlannedWorkout? = null): Response<PlannedWorkout>

    /**
     * GET api/planned-workouts/today
     * Get today&#39;s planned workouts
     * Get planned workouts scheduled for today
     * Responses:
     *  - 200: List of today's planned workouts
     *  - 401: Unauthorized
     *
     * @return [kotlin.collections.List<PlannedWorkout>]
     */
    @GET("api/planned-workouts/today")
    suspend fun apiPlannedWorkoutsTodayGet(): Response<List<PlannedWorkout>>


    /**
    * enum for parameter weekday
    */
    enum class WeekdayApiPlannedWorkoutsWeekdayWeekdayGet(val value: String) {
        @Json(name = "sun") sun("sun"),
        @Json(name = "mon") mon("mon"),
        @Json(name = "tue") tue("tue"),
        @Json(name = "wed") wed("wed"),
        @Json(name = "thu") thu("thu"),
        @Json(name = "fri") fri("fri"),
        @Json(name = "sat") sat("sat")
    }

    /**
     * GET api/planned-workouts/weekday/{weekday}
     * Get planned workouts for weekday
     * Get planned workouts for a specific weekday
     * Responses:
     *  - 200: List of planned workouts for the weekday
     *  - 400: Invalid weekday parameter
     *  - 401: Unauthorized
     *
     * @param weekday Day of the week
     * @return [kotlin.collections.List<PlannedWorkout>]
     */
    @GET("api/planned-workouts/weekday/{weekday}")
    suspend fun apiPlannedWorkoutsWeekdayWeekdayGet(@Path("weekday") weekday: String): Response<List<PlannedWorkout>>

}
