package com.example.cheddyapp

import androidx.room.*
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

    @Query("SELECT * FROM UserTickerData WHERE accountName = :accountName ORDER BY folioWeight DESC")
    fun getData(accountName: String): List<UserTickerData>

    @Query("DELETE FROM UserTickerData WHERE accountName = :accountName")
    fun removeTickers(accountName: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertData(data: UserTickerData)
}
