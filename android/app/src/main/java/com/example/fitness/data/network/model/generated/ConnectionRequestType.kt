/**
 *
 * Please note:
 * This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 *
 */

@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport"
)

package com.example.fitness.data.network.model.generated


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 
 *
 * @param type Type of connection requests to retrieve
 */


data class ConnectionRequestType (

    /* Type of connection requests to retrieve */
    @Json(name = "type")
    val type: ConnectionRequestType.Type

) {

    /**
     * Type of connection requests to retrieve
     *
     * Values: sent,received
     */
    @JsonClass(generateAdapter = false)
    enum class Type(val value: kotlin.String) {
        @Json(name = "sent") sent("sent"),
        @Json(name = "received") received("received");
    }

}

