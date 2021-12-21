package com.example.cheddyapp

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface StockService  {
    @GET
    fun getSingleCurPrice(@Url url: String): Call<JsonObject>
}
