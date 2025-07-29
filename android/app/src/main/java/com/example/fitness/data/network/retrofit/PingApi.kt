package com.example.fitness.data.network.retrofit

import retrofit2.http.*
import retrofit2.Response

import com.example.fitness.data.network.model.generated.PingResponse

interface PingApi {
    /**
     * GET api/ping
     * Health check endpoint
     * Simple health check that returns pong
     * Responses:
     *  - 200: Successful ping response
     *
     * @return [PingResponse]
     */
    @GET("api/ping")
    suspend fun apiPingGet(): Response<PingResponse>

}
