package com.example.fitness.data.network.retrofit

import retrofit2.http.*
import retrofit2.Response

import com.example.fitness.data.network.model.generated.ExerciseType

interface ExercisesApi {
    /**
     * GET api/exercises
     * Get all exercise types
     * Retrieve a list of all available exercise types
     * Responses:
     *  - 200: List of exercise types
     *  - 401: Unauthorized
     *
     * @return [kotlin.collections.List<ExerciseType>]
     */
    @GET("api/exercises")
    suspend fun apiExercisesGet(): Response<List<ExerciseType>>

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
     * @return [ExerciseType]
     */
    @GET("api/exercises/{id}")
    suspend fun apiExercisesIdGet(@Path("id") id: String): Response<ExerciseType>

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
     * @return [ExerciseType]
     */
    @GET("api/exercises/name/{name}")
    suspend fun apiExercisesNameNameGet(@Path("name") name: String): Response<ExerciseType>

}
