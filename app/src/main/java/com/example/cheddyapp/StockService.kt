package com.example.cheddyapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface StockService  {

    @GET
    fun getSingleCurPrice(@Url url: String): Call<JsonObject>
}

@Dao
interface UserDataDao {

    @Query("SELECT * FROM UserTickerData WHERE accountName = :accountName")
    fun getData(accountName: String): List<UserTickerData>

    @Insert
    fun insertData(data: UserTickerData)

    @Update
    fun updateData(data: UserTickerData)
}
