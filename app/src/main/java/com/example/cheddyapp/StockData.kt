package com.example.cheddyapp
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class Ticker   (
    val ticker: String
    )

data class StockData    (
    @SerializedName("lastPrice") val lastPrice: Double,
    @SerializedName("netChange") val netChange: Double,
    @SerializedName("description") val description: String
    )