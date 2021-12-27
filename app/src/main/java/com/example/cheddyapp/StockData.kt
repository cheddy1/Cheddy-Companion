package com.example.cheddyapp

import com.google.gson.annotations.SerializedName

data class StockData    (
    @SerializedName("lastPrice") val lastPrice: Double,
    @SerializedName("netChange") val netChange: Double,
    @SerializedName("description") val description: String
    )

data class BrokerStockOwned (
    val VTI: Double,
    val VXUS: Double,
    val VIOV: Double,
    val QQQM: Double,
    val QQQJ: Double
        )