package com.example.cheddyapp

import androidx.room.Entity
import com.google.gson.annotations.SerializedName

data class StockData    (
    @SerializedName("lastPrice") val lastPrice: Double,
    @SerializedName("netChange") val netChange: Double,
    @SerializedName("description") val description: String
    )

@Entity(primaryKeys = ["accountName", "ticker"])
data class UserTickerData(
    val accountName: String,
    val ticker: String,
    val amountOwned: Double,
    val folioWeight: Int
)