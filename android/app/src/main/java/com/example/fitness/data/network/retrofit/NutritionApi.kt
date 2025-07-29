package com.example.fitness.data.network.retrofit

import retrofit2.http.*
import retrofit2.Response

import com.example.fitness.data.network.model.generated.CreateNutritionPlan
import com.example.fitness.data.network.model.generated.CreateNutritionPlanDay
import com.example.fitness.data.network.model.generated.CreateNutritionPlanFood
import com.example.fitness.data.network.model.generated.CreateNutritionPlanMeal
import com.example.fitness.data.network.model.generated.NutritionPlan
import com.example.fitness.data.network.model.generated.NutritionPlanDay
import com.example.fitness.data.network.model.generated.NutritionPlanFood
import com.example.fitness.data.network.model.generated.NutritionPlanMeal
import com.example.fitness.data.network.model.generated.SuccessMessage
import com.example.fitness.data.network.model.generated.UpdateNutritionPlan
import com.example.fitness.data.network.model.generated.UpdateNutritionPlanDay
import com.example.fitness.data.network.model.generated.UpdateNutritionPlanFood
import com.example.fitness.data.network.model.generated.UpdateNutritionPlanMeal

interface NutritionApi {
    /**
     * DELETE api/nutrition/days/{id}
     * Delete nutrition plan day
     * Delete a nutrition plan day
     * Responses:
     *  - 200: Nutrition plan day deleted successfully
     *  - 401: Unauthorized
     *  - 404: Nutrition plan day not found
     *
     * @param id Unique identifier
     * @return [SuccessMessage]
     */
    @DELETE("api/nutrition/days/{id}")
    suspend fun apiNutritionDaysIdDelete(@Path("id") id: String): Response<SuccessMessage>

    /**
     * GET api/nutrition/days/{id}
     * Get nutrition plan day
     * Get a specific nutrition plan day by ID
     * Responses:
     *  - 200: Nutrition plan day details
     *  - 401: Unauthorized
     *  - 404: Nutrition plan day not found
     *
     * @param id Unique identifier
     * @return [NutritionPlanDay]
     */
    @GET("api/nutrition/days/{id}")
    suspend fun apiNutritionDaysIdGet(@Path("id") id: String): Response<NutritionPlanDay>

    /**
     * GET api/nutrition/days/{id}/meals
     * Get nutrition plan day meals
     * Get all meals for a nutrition plan day
     * Responses:
     *  - 200: List of nutrition plan meals
     *  - 401: Unauthorized
     *  - 404: Nutrition plan day not found
     *
     * @param id Unique identifier
     * @return [kotlin.collections.List<NutritionPlanMeal>]
     */
    @GET("api/nutrition/days/{id}/meals")
    suspend fun apiNutritionDaysIdMealsGet(@Path("id") id: String): Response<List<NutritionPlanMeal>>

    /**
     * POST api/nutrition/days/{id}/meals
     * Create nutrition plan meal
     * Create a new meal for a nutrition plan day
     * Responses:
     *  - 201: Nutrition plan meal created successfully
     *  - 400: Invalid input data
     *  - 401: Unauthorized
     *
     * @param id Unique identifier
     * @param createNutritionPlanMeal Nutrition plan meal data (optional)
     * @return [NutritionPlanMeal]
     */
    @POST("api/nutrition/days/{id}/meals")
    suspend fun apiNutritionDaysIdMealsPost(@Path("id") id: String, @Body createNutritionPlanMeal: CreateNutritionPlanMeal? = null): Response<NutritionPlanMeal>

    /**
     * PUT api/nutrition/days/{id}
     * Update nutrition plan day
     * Update a nutrition plan day
     * Responses:
     *  - 200: Nutrition plan day updated successfully
     *  - 400: Invalid input data
     *  - 401: Unauthorized
     *  - 404: Nutrition plan day not found
     *
     * @param id Unique identifier
     * @param updateNutritionPlanDay Updated nutrition plan day data (optional)
     * @return [NutritionPlanDay]
     */
    @PUT("api/nutrition/days/{id}")
    suspend fun apiNutritionDaysIdPut(@Path("id") id: String, @Body updateNutritionPlanDay: UpdateNutritionPlanDay? = null): Response<NutritionPlanDay>

    /**
     * DELETE api/nutrition/foods/{id}
     * Delete nutrition plan food
     * Delete a nutrition plan food
     * Responses:
     *  - 200: Nutrition plan food deleted successfully
     *  - 401: Unauthorized
     *  - 404: Nutrition plan food not found
     *
     * @param id Unique identifier
     * @return [SuccessMessage]
     */
    @DELETE("api/nutrition/foods/{id}")
    suspend fun apiNutritionFoodsIdDelete(@Path("id") id: String): Response<SuccessMessage>

    /**
     * GET api/nutrition/foods/{id}
     * Get nutrition plan food
     * Get a specific nutrition plan food by ID
     * Responses:
     *  - 200: Nutrition plan food details
     *  - 401: Unauthorized
     *  - 404: Nutrition plan food not found
     *
     * @param id Unique identifier
     * @return [NutritionPlanFood]
     */
    @GET("api/nutrition/foods/{id}")
    suspend fun apiNutritionFoodsIdGet(@Path("id") id: String): Response<NutritionPlanFood>

    /**
     * PUT api/nutrition/foods/{id}
     * Update nutrition plan food
     * Update a nutrition plan food
     * Responses:
     *  - 200: Nutrition plan food updated successfully
     *  - 400: Invalid input data
     *  - 401: Unauthorized
     *  - 404: Nutrition plan food not found
     *
     * @param id Unique identifier
     * @param updateNutritionPlanFood Updated nutrition plan food data (optional)
     * @return [NutritionPlanFood]
     */
    @PUT("api/nutrition/foods/{id}")
    suspend fun apiNutritionFoodsIdPut(@Path("id") id: String, @Body updateNutritionPlanFood: UpdateNutritionPlanFood? = null): Response<NutritionPlanFood>

    /**
     * GET api/nutrition
     * Get all nutrition plans
     * Get all nutrition plans accessible to the current user
     * Responses:
     *  - 200: List of nutrition plans
     *  - 401: Unauthorized
     *
     * @return [kotlin.collections.List<NutritionPlan>]
     */
    @GET("api/nutrition")
    suspend fun apiNutritionGet(): Response<List<NutritionPlan>>

    /**
     * GET api/nutrition/{id}/days
     * Get nutrition plan days
     * Get all days for a nutrition plan
     * Responses:
     *  - 200: List of nutrition plan days
     *  - 401: Unauthorized
     *  - 404: Nutrition plan not found
     *
     * @param id Unique identifier
     * @return [kotlin.collections.List<NutritionPlanDay>]
     */
    @GET("api/nutrition/{id}/days")
    suspend fun apiNutritionIdDaysGet(@Path("id") id: String): Response<List<NutritionPlanDay>>

    /**
     * POST api/nutrition/{id}/days
     * Create nutrition plan day
     * Create a new day for a nutrition plan
     * Responses:
     *  - 201: Nutrition plan day created successfully
     *  - 400: Invalid input data
     *  - 401: Unauthorized
     *
     * @param id Unique identifier
     * @param createNutritionPlanDay Nutrition plan day data (optional)
     * @return [NutritionPlanDay]
     */
    @POST("api/nutrition/{id}/days")
    suspend fun apiNutritionIdDaysPost(@Path("id") id: String, @Body createNutritionPlanDay: CreateNutritionPlanDay? = null): Response<NutritionPlanDay>

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
     * @return [SuccessMessage]
     */
    @DELETE("api/nutrition/{id}")
    suspend fun apiNutritionIdDelete(@Path("id") id: String): Response<SuccessMessage>

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
     * @return [NutritionPlan]
     */
    @GET("api/nutrition/{id}")
    suspend fun apiNutritionIdGet(@Path("id") id: String): Response<NutritionPlan>

    /**
     * PUT api/nutrition/{id}
     * Update nutrition plan
     * Update a nutrition plan (coach only)
     * Responses:
     *  - 200: Nutrition plan updated successfully
     *  - 400: Invalid input data
     *  - 401: Unauthorized - Coach role required
     *  - 404: Nutrition plan not found
     *
     * @param id Unique identifier
     * @param updateNutritionPlan Updated nutrition plan data (optional)
     * @return [NutritionPlan]
     */
    @PUT("api/nutrition/{id}")
    suspend fun apiNutritionIdPut(@Path("id") id: String, @Body updateNutritionPlan: UpdateNutritionPlan? = null): Response<NutritionPlan>

    /**
     * DELETE api/nutrition/meals/{id}
     * Delete nutrition plan meal
     * Delete a nutrition plan meal
     * Responses:
     *  - 200: Nutrition plan meal deleted successfully
     *  - 401: Unauthorized
     *  - 404: Nutrition plan meal not found
     *
     * @param id Unique identifier
     * @return [SuccessMessage]
     */
    @DELETE("api/nutrition/meals/{id}")
    suspend fun apiNutritionMealsIdDelete(@Path("id") id: String): Response<SuccessMessage>

    /**
     * GET api/nutrition/meals/{id}/foods
     * Get nutrition plan meal foods
     * Get all foods for a nutrition plan meal
     * Responses:
     *  - 200: List of nutrition plan foods
     *  - 401: Unauthorized
     *  - 404: Nutrition plan meal not found
     *
     * @param id Unique identifier
     * @return [kotlin.collections.List<NutritionPlanFood>]
     */
    @GET("api/nutrition/meals/{id}/foods")
    suspend fun apiNutritionMealsIdFoodsGet(@Path("id") id: String): Response<List<NutritionPlanFood>>

    /**
     * POST api/nutrition/meals/{id}/foods
     * Create nutrition plan food
     * Create a new food for a nutrition plan meal
     * Responses:
     *  - 201: Nutrition plan food created successfully
     *  - 400: Invalid input data
     *  - 401: Unauthorized
     *
     * @param id Unique identifier
     * @param createNutritionPlanFood Nutrition plan food data (optional)
     * @return [NutritionPlanFood]
     */
    @POST("api/nutrition/meals/{id}/foods")
    suspend fun apiNutritionMealsIdFoodsPost(@Path("id") id: String, @Body createNutritionPlanFood: CreateNutritionPlanFood? = null): Response<NutritionPlanFood>

    /**
     * GET api/nutrition/meals/{id}
     * Get nutrition plan meal
     * Get a specific nutrition plan meal by ID
     * Responses:
     *  - 200: Nutrition plan meal details
     *  - 401: Unauthorized
     *  - 404: Nutrition plan meal not found
     *
     * @param id Unique identifier
     * @return [NutritionPlanMeal]
     */
    @GET("api/nutrition/meals/{id}")
    suspend fun apiNutritionMealsIdGet(@Path("id") id: String): Response<NutritionPlanMeal>

    /**
     * PUT api/nutrition/meals/{id}
     * Update nutrition plan meal
     * Update a nutrition plan meal
     * Responses:
     *  - 200: Nutrition plan meal updated successfully
     *  - 400: Invalid input data
     *  - 401: Unauthorized
     *  - 404: Nutrition plan meal not found
     *
     * @param id Unique identifier
     * @param updateNutritionPlanMeal Updated nutrition plan meal data (optional)
     * @return [NutritionPlanMeal]
     */
    @PUT("api/nutrition/meals/{id}")
    suspend fun apiNutritionMealsIdPut(@Path("id") id: String, @Body updateNutritionPlanMeal: UpdateNutritionPlanMeal? = null): Response<NutritionPlanMeal>

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
     * @return [NutritionPlan]
     */
    @POST("api/nutrition")
    suspend fun apiNutritionPost(@Body createNutritionPlan: CreateNutritionPlan? = null): Response<NutritionPlan>

}
