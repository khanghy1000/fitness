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
 * @param weight Body weight in kg
 * @param height Height in cm
 * @param bodyFat Body fat percentage
 * @param muscleMass Muscle mass in kg
 * @param chest Chest measurement in cm
 * @param waist Waist measurement in cm
 * @param hips Hip measurement in cm
 * @param arms Arm measurement in cm
 * @param thighs Thigh measurement in cm
 * @param notes Additional notes about measurements
 */


data class RecordUserStats (

    /* Body weight in kg */
    @Json(name = "weight")
    val weight: java.math.BigDecimal? = null,

    /* Height in cm */
    @Json(name = "height")
    val height: java.math.BigDecimal? = null,

    /* Body fat percentage */
    @Json(name = "bodyFat")
    val bodyFat: java.math.BigDecimal? = null,

    /* Muscle mass in kg */
    @Json(name = "muscleMass")
    val muscleMass: java.math.BigDecimal? = null,

    /* Chest measurement in cm */
    @Json(name = "chest")
    val chest: java.math.BigDecimal? = null,

    /* Waist measurement in cm */
    @Json(name = "waist")
    val waist: java.math.BigDecimal? = null,

    /* Hip measurement in cm */
    @Json(name = "hips")
    val hips: java.math.BigDecimal? = null,

    /* Arm measurement in cm */
    @Json(name = "arms")
    val arms: java.math.BigDecimal? = null,

    /* Thigh measurement in cm */
    @Json(name = "thighs")
    val thighs: java.math.BigDecimal? = null,

    /* Additional notes about measurements */
    @Json(name = "notes")
    val notes: kotlin.String? = null

) {


}

