package com.example.fitness.data.network.adapter;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

import java.math.BigDecimal;

public class BigDecimalAdapter {
    
    @ToJson
    public String toJson(BigDecimal value) {
        return value != null ? value.toString() : null;
    }
    
    @FromJson
    public BigDecimal fromJson(String value) {
        return value != null ? new BigDecimal(value) : null;
    }
}
