package com.example.fitness.data.network.retrofit

import com.example.fitness.data.network.model.generated.*
import com.squareup.moshi.Json
import retrofit2.http.*
import retrofit2.Call

interface PlannedWorkoutsApi {
    /**
     * GET api/planned-workouts
     * Get user planned workouts
     * Get all planned workouts for the current user
     * Responses:
     *  - 200: List of planned workouts
     *  - 401: Unauthorized
     *
     * @return [Call]<[kotlin.collections.List<PlannedWorkout>]>
     */
    @GET("api/planned-workouts")
    fun apiPlannedWorkoutsGet(): Call<List<PlannedWorkout>>

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
     * @return [Call]<[SuccessMessage]>
     */
    @DELETE("api/planned-workouts/{id}")
    fun apiPlannedWorkoutsIdDelete(@Path("id") id: String): Call<SuccessMessage>

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
     * @return [Call]<[PlannedWorkout]>
     */
    @GET("api/planned-workouts/{id}")
    fun apiPlannedWorkoutsIdGet(@Path("id") id: String): Call<PlannedWorkout>

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
     * @return [Call]<[PlannedWorkout]>
     */
    @PUT("api/planned-workouts/{id}")
    fun apiPlannedWorkoutsIdPut(@Path("id") id: String, @Body updatePlannedWorkout: UpdatePlannedWorkout? = null): Call<PlannedWorkout>

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
     * @return [Call]<[PlannedWorkout]>
     */
    @POST("api/planned-workouts/{id}/toggle")
    fun apiPlannedWorkoutsIdTogglePost(@Path("id") id: String, @Body togglePlannedWorkout: TogglePlannedWorkout? = null): Call<PlannedWorkout>

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
     * @return [Call]<[PlannedWorkout]>
     */
    @POST("api/planned-workouts")
    fun apiPlannedWorkoutsPost(@Body createPlannedWorkout: CreatePlannedWorkout? = null): Call<PlannedWorkout>

    /**
     * GET api/planned-workouts/today
     * Get today&#39;s planned workouts
     * Get planned workouts scheduled for today
     * Responses:
     *  - 200: List of today's planned workouts
     *  - 401: Unauthorized
     *
     * @return [Call]<[kotlin.collections.List<PlannedWorkout>]>
     */
    @GET("api/planned-workouts/today")
    fun apiPlannedWorkoutsTodayGet(): Call<List<PlannedWorkout>>


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
     * @return [Call]<[kotlin.collections.List<PlannedWorkout>]>
     */
    @GET("api/planned-workouts/weekday/{weekday}")
    fun apiPlannedWorkoutsWeekdayWeekdayGet(@Path("weekday") weekday: String): Call<List<PlannedWorkout>>

}
