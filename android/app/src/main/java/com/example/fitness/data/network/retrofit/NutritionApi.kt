package com.example.fitness.data.network.retrofit

import com.example.fitness.data.network.model.generated.*
import com.squareup.moshi.Json
import retrofit2.http.*
import retrofit2.Call

interface NutritionApi {
    /**
     * GET api/nutrition
     * Get all nutrition plans
     * Get all nutrition plans accessible to the current user
     * Responses:
     *  - 200: List of nutrition plans
     *  - 401: Unauthorized
     *
     * @return [Call]<[kotlin.collections.List<NutritionPlan>]>
     */
    @GET("api/nutrition")
    fun apiNutritionGet(): Call<kotlin.collections.List<NutritionPlan>>

    /**
     * PUT api/nutrition/{id}/bulk
     * Bulk update nutrition plan
     * Update a nutrition plan with all its days, meals, foods in one request. Items not included will be deleted.
     * Responses:
     *  - 200: Nutrition plan updated successfully
     *  - 400: Invalid input data
     *  - 401: Unauthorized
     *  - 404: Nutrition plan not found
     *  - 500: Internal server error
     *
     * @param id Unique identifier
     * @param bulkUpdateNutritionPlan Complete nutrition plan data (optional)
     * @return [Call]<[DetailedNutritionPlan]>
     */
    @PUT("api/nutrition/{id}/bulk")
    fun apiNutritionIdBulkPut(@Path("id") id: kotlin.String, @Body bulkUpdateNutritionPlan: BulkUpdateNutritionPlan? = null): Call<DetailedNutritionPlan>

    /**
     * DELETE api/nutrition/{id}
     * Delete nutrition plan
     * Delete a nutrition plan (coach only)
     * Responses:
     *  - 200: Nutrition plan deleted successfully
     *  - 401: Unauthorized - Coach role required
     *  - 404: Nutrition plan not found
     *
     * @param id Unique identifier
     * @return [Call]<[SuccessMessage]>
     */
    @DELETE("api/nutrition/{id}")
    fun apiNutritionIdDelete(@Path("id") id: kotlin.String): Call<SuccessMessage>

    /**
     * GET api/nutrition/{id}
     * Get nutrition plan by ID
     * Get a specific nutrition plan with full details
     * Responses:
     *  - 200: Nutrition plan details
     *  - 401: Unauthorized
     *  - 404: Nutrition plan not found
     *
     * @param id Unique identifier
     * @return [Call]<[DetailedNutritionPlan]>
     */
    @GET("api/nutrition/{id}")
    fun apiNutritionIdGet(@Path("id") id: kotlin.String): Call<DetailedNutritionPlan>

    /**
     * POST api/nutrition
     * Create nutrition plan
     * Create a new nutrition plan
     * Responses:
     *  - 201: Nutrition plan created successfully
     *  - 400: Invalid input data
     *  - 401: Unauthorized
     *
     * @param createNutritionPlan Nutrition plan data (optional)
     * @return [Call]<[NutritionPlan]>
     */
    @POST("api/nutrition")
    fun apiNutritionPost(@Body createNutritionPlan: CreateNutritionPlan? = null): Call<NutritionPlan>

}
