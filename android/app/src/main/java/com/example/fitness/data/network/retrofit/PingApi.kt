package com.example.fitness.data.network.retrofit

import com.example.fitness.data.network.model.generated.*
import com.squareup.moshi.Json
import retrofit2.http.*
import retrofit2.Call

interface PingApi {
    /**
     * GET api/ping
     * Health check endpoint
     * Simple health check that returns pong
     * Responses:
     *  - 200: Successful ping response
     *
     * @return [Call]<[PingResponse]>
     */
    @GET("api/ping")
    fun apiPingGet(): Call<PingResponse>

}
