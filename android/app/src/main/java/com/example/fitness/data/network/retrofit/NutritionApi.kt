package com.example.fitness.data.network.retrofit

import com.example.fitness.data.network.model.generated.*
import com.squareup.moshi.Json
import retrofit2.http.*
import retrofit2.Call

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
     * @return [Call]<[SuccessMessage]>
     */
    @DELETE("api/nutrition/days/{id}")
    fun apiNutritionDaysIdDelete(@Path("id") id: kotlin.String): Call<SuccessMessage>

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
     * @return [Call]<[NutritionPlanDay]>
     */
    @GET("api/nutrition/days/{id}")
    fun apiNutritionDaysIdGet(@Path("id") id: kotlin.String): Call<NutritionPlanDay>

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
     * @return [Call]<[kotlin.collections.List<NutritionPlanMeal>]>
     */
    @GET("api/nutrition/days/{id}/meals")
    fun apiNutritionDaysIdMealsGet(@Path("id") id: kotlin.String): Call<kotlin.collections.List<NutritionPlanMeal>>

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
     * @return [Call]<[NutritionPlanMeal]>
     */
    @POST("api/nutrition/days/{id}/meals")
    fun apiNutritionDaysIdMealsPost(@Path("id") id: kotlin.String, @Body createNutritionPlanMeal: CreateNutritionPlanMeal? = null): Call<NutritionPlanMeal>

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
     * @return [Call]<[NutritionPlanDay]>
     */
    @PUT("api/nutrition/days/{id}")
    fun apiNutritionDaysIdPut(@Path("id") id: kotlin.String, @Body updateNutritionPlanDay: UpdateNutritionPlanDay? = null): Call<NutritionPlanDay>

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
     * @return [Call]<[SuccessMessage]>
     */
    @DELETE("api/nutrition/foods/{id}")
    fun apiNutritionFoodsIdDelete(@Path("id") id: kotlin.String): Call<SuccessMessage>

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
     * @return [Call]<[NutritionPlanFood]>
     */
    @GET("api/nutrition/foods/{id}")
    fun apiNutritionFoodsIdGet(@Path("id") id: kotlin.String): Call<NutritionPlanFood>

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
     * @return [Call]<[NutritionPlanFood]>
     */
    @PUT("api/nutrition/foods/{id}")
    fun apiNutritionFoodsIdPut(@Path("id") id: kotlin.String, @Body updateNutritionPlanFood: UpdateNutritionPlanFood? = null): Call<NutritionPlanFood>

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
     * GET api/nutrition/{id}/days
     * Get nutrition plan days
     * Get all days for a nutrition plan
     * Responses:
     *  - 200: List of nutrition plan days
     *  - 401: Unauthorized
     *  - 404: Nutrition plan not found
     *
     * @param id Unique identifier
     * @return [Call]<[kotlin.collections.List<NutritionPlanDay>]>
     */
    @GET("api/nutrition/{id}/days")
    fun apiNutritionIdDaysGet(@Path("id") id: kotlin.String): Call<kotlin.collections.List<NutritionPlanDay>>

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
     * @return [Call]<[NutritionPlanDay]>
     */
    @POST("api/nutrition/{id}/days")
    fun apiNutritionIdDaysPost(@Path("id") id: kotlin.String, @Body createNutritionPlanDay: CreateNutritionPlanDay? = null): Call<NutritionPlanDay>

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
     * @return [Call]<[NutritionPlan]>
     */
    @GET("api/nutrition/{id}")
    fun apiNutritionIdGet(@Path("id") id: kotlin.String): Call<NutritionPlan>

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
     * @return [Call]<[NutritionPlan]>
     */
    @PUT("api/nutrition/{id}")
    fun apiNutritionIdPut(@Path("id") id: kotlin.String, @Body updateNutritionPlan: UpdateNutritionPlan? = null): Call<NutritionPlan>

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
     * @return [Call]<[SuccessMessage]>
     */
    @DELETE("api/nutrition/meals/{id}")
    fun apiNutritionMealsIdDelete(@Path("id") id: kotlin.String): Call<SuccessMessage>

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
     * @return [Call]<[kotlin.collections.List<NutritionPlanFood>]>
     */
    @GET("api/nutrition/meals/{id}/foods")
    fun apiNutritionMealsIdFoodsGet(@Path("id") id: kotlin.String): Call<kotlin.collections.List<NutritionPlanFood>>

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
     * @return [Call]<[NutritionPlanFood]>
     */
    @POST("api/nutrition/meals/{id}/foods")
    fun apiNutritionMealsIdFoodsPost(@Path("id") id: kotlin.String, @Body createNutritionPlanFood: CreateNutritionPlanFood? = null): Call<NutritionPlanFood>

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
     * @return [Call]<[NutritionPlanMeal]>
     */
    @GET("api/nutrition/meals/{id}")
    fun apiNutritionMealsIdGet(@Path("id") id: kotlin.String): Call<NutritionPlanMeal>

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
     * @return [Call]<[NutritionPlanMeal]>
     */
    @PUT("api/nutrition/meals/{id}")
    fun apiNutritionMealsIdPut(@Path("id") id: kotlin.String, @Body updateNutritionPlanMeal: UpdateNutritionPlanMeal? = null): Call<NutritionPlanMeal>

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
