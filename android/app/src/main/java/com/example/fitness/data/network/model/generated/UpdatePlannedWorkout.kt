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
 * @param weekdays Updated days of the week
 * @param time Updated time for the workout
 * @param isActive Updated active status
 */


data class UpdatePlannedWorkout (

    /* Updated days of the week */
    @Json(name = "weekdays")
    val weekdays: kotlin.collections.List<UpdatePlannedWorkout.Weekdays>? = null,

    /* Updated time for the workout */
    @Json(name = "time")
    val time: kotlin.String? = null,

    /* Updated active status */
    @Json(name = "isActive")
    val isActive: kotlin.Boolean? = null

) {

    /**
     * Updated days of the week
     *
     * Values: sun,mon,tue,wed,thu,fri,sat
     */
    @JsonClass(generateAdapter = false)
    enum class Weekdays(val value: kotlin.String) {
        @Json(name = "sun") sun("sun"),
        @Json(name = "mon") mon("mon"),
        @Json(name = "tue") tue("tue"),
        @Json(name = "wed") wed("wed"),
        @Json(name = "thu") thu("thu"),
        @Json(name = "fri") fri("fri"),
        @Json(name = "sat") sat("sat");
    }

}

