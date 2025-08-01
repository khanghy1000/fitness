package com.example.fitness.data.network.retrofit

import com.example.fitness.data.network.model.generated.*
import com.squareup.moshi.Json
import retrofit2.http.*
import retrofit2.Call

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
     * @return [Call]<[ConnectionWithoutCoachTrainee]>
     */
    @POST("api/connections/accept")
    fun apiConnectionsAcceptPost(@Body traineeId: TraineeId? = null): Call<ConnectionWithoutCoachTrainee>

    /**
     * GET api/connections/active
     * Get active connections
     * Get list of active connections for the current user
     * Responses:
     *  - 200: List of active connections
     *  - 401: Unauthorized
     *  - 403: Access denied
     *
     * @return [Call]<[kotlin.collections.List<Connection>]>
     */
    @GET("api/connections/active")
    fun apiConnectionsActiveGet(): Call<kotlin.collections.List<Connection>>

    /**
     * GET api/connections/all
     * Get all connections
     * Get list of all connections (all statuses) for the current user
     * Responses:
     *  - 200: List of all connections
     *  - 401: Unauthorized
     *  - 403: Access denied
     *
     * @return [Call]<[kotlin.collections.List<Connection>]>
     */
    @GET("api/connections/all")
    fun apiConnectionsAllGet(): Call<kotlin.collections.List<Connection>>

    /**
     * POST api/connections/connect
     * Send connection request
     * Send a connection request from trainee to coach
     * Responses:
     *  - 201: Connection request sent successfully
     *  - 400: Invalid input data or connection already exists
     *  - 401: Unauthorized - Trainee role required
     *
     * @param connectRequest Connection request data (optional)
     * @return [Call]<[ConnectionWithoutCoachTrainee]>
     */
    @POST("api/connections/connect")
    fun apiConnectionsConnectPost(@Body connectRequest: ConnectRequest? = null): Call<ConnectionWithoutCoachTrainee>

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
     * @return [Call]<[SuccessMessage]>
     */
    @POST("api/connections/disconnect")
    fun apiConnectionsDisconnectPost(@Body traineeId: TraineeId? = null): Call<SuccessMessage>

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
     * @return [Call]<[SuccessMessage]>
     */
    @POST("api/connections/reject")
    fun apiConnectionsRejectPost(@Body traineeId: TraineeId? = null): Call<SuccessMessage>


    /**
    * enum for parameter type
    */
    enum class TypeApiConnectionsRequestsTypeGet(val value: kotlin.String) {
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
     * @return [Call]<[kotlin.collections.List<Connection>]>
     */
    @GET("api/connections/requests/{type}")
    fun apiConnectionsRequestsTypeGet(@Path("type") type: kotlin.String): Call<kotlin.collections.List<Connection>>

}
