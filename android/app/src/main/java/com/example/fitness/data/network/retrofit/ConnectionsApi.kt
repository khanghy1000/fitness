package com.example.fitness.data.network.retrofit

import retrofit2.http.*
import retrofit2.Response
import com.squareup.moshi.Json

import com.example.fitness.data.network.model.generated.ConnectRequest
import com.example.fitness.data.network.model.generated.Connection
import com.example.fitness.data.network.model.generated.SuccessMessage
import com.example.fitness.data.network.model.generated.TraineeId

interface ConnectionsApi {
    /**
     * POST api/connections/accept
     * Accept connection request
     * Accept a connection request (coach only)
     * Responses:
     *  - 200: Connection request accepted
     *  - 400: Invalid input data
     *  - 401: Unauthorized - Coach role required
     *  - 404: Connection request not found
     *
     * @param traineeId Trainee ID to accept (optional)
     * @return [Connection]
     */
    @POST("api/connections/accept")
    suspend fun apiConnectionsAcceptPost(@Body traineeId: TraineeId? = null): Response<Connection>

    /**
     * POST api/connections/connect
     * Send connection request
     * Send a connection request from trainee to coach
     * Responses:
     *  - 201: Connection request sent successfully
     *  - 400: Invalid input data
     *  - 401: Unauthorized - Trainee role required
     *
     * @param connectRequest Connection request data (optional)
     * @return [Connection]
     */
    @POST("api/connections/connect")
    suspend fun apiConnectionsConnectPost(@Body connectRequest: ConnectRequest? = null): Response<Connection>

    /**
     * GET api/connections/connections
     * Get active connections
     * Get list of active connections for the current user
     * Responses:
     *  - 200: List of active connections
     *  - 401: Unauthorized
     *  - 403: Access denied
     *
     * @return [kotlin.collections.List<Connection>]
     */
    @GET("api/connections/connections")
    suspend fun apiConnectionsConnectionsGet(): Response<List<Connection>>

    /**
     * POST api/connections/disconnect
     * End connection
     * End an active connection (coach only)
     * Responses:
     *  - 200: Connection ended successfully
     *  - 400: Invalid input data
     *  - 401: Unauthorized - Coach role required
     *  - 404: Active connection not found
     *
     * @param traineeId Trainee ID to disconnect (optional)
     * @return [SuccessMessage]
     */
    @POST("api/connections/disconnect")
    suspend fun apiConnectionsDisconnectPost(@Body traineeId: TraineeId? = null): Response<SuccessMessage>

    /**
     * POST api/connections/reject
     * Reject connection request
     * Reject a connection request (coach only)
     * Responses:
     *  - 200: Connection request rejected
     *  - 400: Invalid input data
     *  - 401: Unauthorized - Coach role required
     *  - 404: Connection request not found
     *
     * @param traineeId Trainee ID to reject (optional)
     * @return [SuccessMessage]
     */
    @POST("api/connections/reject")
    suspend fun apiConnectionsRejectPost(@Body traineeId: TraineeId? = null): Response<SuccessMessage>


    /**
    * enum for parameter type
    */
    enum class TypeApiConnectionsRequestsTypeGet(val value: String) {
        @Json(name = "sent") sent("sent"),
        @Json(name = "received") received("received")
    }

    /**
     * GET api/connections/requests/{type}
     * Get connection requests
     * Get connection requests (sent or received)
     * Responses:
     *  - 200: List of connection requests
     *  - 401: Unauthorized
     *  - 403: Access denied - Role mismatch
     *
     * @param type Type of connection requests to retrieve
     * @return [kotlin.collections.List<Connection>]
     */
    @GET("api/connections/requests/{type}")
    suspend fun apiConnectionsRequestsTypeGet(@Path("type") type: String): Response<List<Connection>>

    /**
     * GET api/connections/trainees
     * Get coach trainees
     * Get list of trainees connected to the coach
     * Responses:
     *  - 200: List of connected trainees
     *  - 401: Unauthorized - Coach role required
     *
     * @return [kotlin.collections.List<Connection>]
     */
    @GET("api/connections/trainees")
    suspend fun apiConnectionsTraineesGet(): Response<List<Connection>>

}
