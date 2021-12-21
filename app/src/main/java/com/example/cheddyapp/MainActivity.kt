package com.example.cheddyapp

import android.os.Bundle
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.example.cheddyapp.ui.main.SectionsPagerAdapter
import com.example.cheddyapp.databinding.ActivityMainBinding
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import retrofit2.Retrofit
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Call
import retrofit2.converter.gson.GsonConverterFactory


private const val BASE_URL = "https://api.tdameritrade.com/v1/marketdata/"
private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {

    private lateinit var curTickerData: JsonObject
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)
        val fab: FloatingActionButton = binding.fab

        val gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create()
        val retrofit = Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create(gson)).build()

        val stockService = retrofit.create(StockService::class.java)
        val ticker = Ticker("TWTR")
        stockService.getSingleCurPrice(BASE_URL+ticker.ticker+"/quotes?apikey=").enqueue(object: Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.i(TAG, "onResponse $response")
                val tickerData = response.body()
                if (tickerData == null){
                    Log.w(TAG, "Did not recieve valid response body")
                    return
                }
                val values = Gson().fromJson(tickerData.get(ticker.ticker), StockData::class.java)
                Log.i(TAG, "onResponse $values")
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.e(TAG, "onFailure $t")
            }

        })
    }
}