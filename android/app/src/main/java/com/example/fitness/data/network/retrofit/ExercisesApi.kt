package com.example.fitness.data.network.retrofit

import com.example.fitness.data.network.model.generated.*
import com.squareup.moshi.Json
import retrofit2.http.*
import retrofit2.Call

interface ExercisesApi {
    /**
     * GET api/exercises
     * Get all exercise types
     * Retrieve a list of all available exercise types
     * Responses:
     *  - 200: List of exercise types
     *  - 401: Unauthorized
     *
     * @return [Call]<[kotlin.collections.List<ExerciseType>]>
     */
    @GET("api/exercises")
    fun apiExercisesGet(): Call<List<ExerciseType>>

    /**
     * GET api/exercises/{id}
     * Get exercise type by ID
     * Retrieve a specific exercise type by its ID
     * Responses:
     *  - 200: Exercise type details
     *  - 401: Unauthorized
     *  - 404: Exercise type not found
     *
     * @param id Unique identifier
     * @return [Call]<[ExerciseType]>
     */
    @GET("api/exercises/{id}")
    fun apiExercisesIdGet(@Path("id") id: String): Call<ExerciseType>

    /**
     * GET api/exercises/name/{name}
     * Get exercise type by name
     * Retrieve a specific exercise type by its name
     * Responses:
     *  - 200: Exercise type details
     *  - 401: Unauthorized
     *  - 404: Exercise type not found
     *
     * @param name Name of the exercise
     * @return [Call]<[ExerciseType]>
     */
    @GET("api/exercises/name/{name}")
    fun apiExercisesNameNameGet(@Path("name") name: String): Call<ExerciseType>

}
