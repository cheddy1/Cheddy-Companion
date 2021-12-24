package com.example.cheddyapp

import com.google.gson.annotations.SerializedName

data class StockData    (
    @SerializedName("lastPrice") val lastPrice: Double,
    @SerializedName("netChange") val netChange: Double,
    @SerializedName("description") val description: String
    )