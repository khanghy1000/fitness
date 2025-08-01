package com.example.fitness.data.network.adapter;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

import java.math.BigDecimal;

public class BigDecimalAdapter {
    @ToJson
    public Double toJson(BigDecimal value) {
        return value != null ? value.doubleValue() : null;
    }

    @FromJson
    public BigDecimal fromJson(Double value) {
        return value != null ? BigDecimal.valueOf(value) : null;
    }
}
