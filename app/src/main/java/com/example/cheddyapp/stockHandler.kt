package com.example.cheddyapp

import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.FragmentActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val BASE_URL = "https://api.tdameritrade.com/v1/marketdata/"
private val brokerTickerList = arrayOf(
    "VTI",
    "VXUS",
    "AVUV",
    "QQQM",
    "QQQJ",
    "ARKF",
    "ICLN",
    "TWTR",
    "ASTS",
    "GENI",
    "TTOO"
)
private val rothTickerList = arrayOf(
    "VTI",
    "VXUS",
    "VIOV",
    "QQQM",
    "QQQJ"
)

class StockDataHandler(private val tab : String, private val activity: FragmentActivity?, private val cLayout: ConstraintLayout, private val swipeContainer: SwipeRefreshLayout) {
    private var index: Int = 0
    private var curValue: Double = 0.0
    val TAG = "StockDataHandler"
    private lateinit var lastPerc: TextView
    private lateinit var lastDesc: TextView
    private lateinit var tickerData: JsonObject
    private lateinit var brokerTickerString: String
    private lateinit var tickerList: Array<String>
    private var amountOwned: Double = 0.0

    fun fetchStockData() {
        val gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create()
        val retrofit = Retrofit.Builder().baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson)).build()
        val stockService = retrofit.create(StockService::class.java)
        tickerList = if (tab == "roth") {
            rothTickerList
        } else {
            brokerTickerList
        }
        brokerTickerString = tickerList.joinToString(separator = "%2C") { it }
        stockService.getSingleCurPrice("$BASE_URL/quotes?apikey=LEAPZSMYRNJADCF7EJNKI6B09BRHGFHD&symbol=$brokerTickerString")
            .enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    Log.i(TAG, "onResponse $response")
                    if (response.body() == null) {
                        Log.w(TAG, "Did not recieve valid response body")
                        return
                    }
                    tickerData = response.body()!!
                    cLayout.removeAllViews()
                    generateTextViews()
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Log.e(TAG, "onFailure $t")
                }

            })
    }
    private fun generateTextViews() {
        index = 0
        curValue = 0.0
        val curValueText = TextView(activity)
        curValueText.textSize = 24F
        curValueText.setTextColor(Color.parseColor("#FFFFFF"))
        curValueText.id = View.generateViewId()

        for (ticker in tickerList) {
            val constraintSet = ConstraintSet()
            val values = Gson().fromJson(tickerData.get(ticker), StockData::class.java)
            if (tab == "roth") {
                when (ticker) {
                    "VTI" -> {
                        amountOwned = 6.0222
                    }
                    "VXUS" -> {
                        amountOwned = 11.0408
                    }
                    "VIOV" -> {
                        amountOwned = 2.0108
                    }
                    "QQQM" -> {
                        amountOwned = 2.0012
                    }
                    "QQQJ" -> {
                        amountOwned = 6.0015
                    }
                }
            }
            else if (tab == "broker") {
                when (ticker) {
                    "VTI" -> {
                        amountOwned = 2.0064
                    }
                    "VXUS" -> {
                        amountOwned = 8.011
                    }
                    "AVUV" -> {
                        amountOwned = 5.0313
                    }
                    "QQQM" -> {
                        amountOwned = 1.0012
                    }
                    "QQQJ" -> {
                        amountOwned = 5.0036
                    }
                    "ARKF" -> {
                        amountOwned = 5.0
                    }
                    "ICLN" -> {
                        amountOwned = 5.044
                    }
                    "TWTR" -> {
                        amountOwned = 3.5481
                    }
                    "ASTS" -> {
                        amountOwned = 13.0
                    }
                    "GENI" -> {
                        amountOwned = 9.0
                    }
                    "TTOO" -> {
                        amountOwned = 130.0
                    }
                }
            }
            curValue += values.lastPrice * amountOwned

            val tickerText = TextView(activity)
            tickerText.text = ticker
            tickerText.setTextColor(Color.parseColor("#FFFFFF"))
            tickerText.textSize = 28F
            tickerText.id = View.generateViewId()
            cLayout.addView(tickerText, index)
            index += 1

            val priceText = TextView(activity)
            val tempPrice = "$" + values.lastPrice.toString()
            priceText.text = tempPrice
            priceText.setTextColor(Color.parseColor("#FFFFFF"))
            priceText.textSize = 28F
            priceText.id = View.generateViewId()
            cLayout.addView(priceText, index)
            index += 1

            val descText = TextView(activity)
            descText.text = values.description
            descText.setTextColor(Color.parseColor("#FFFFFF"))
            descText.textSize = 11F
            descText.id = View.generateViewId()
            cLayout.addView(descText, index)
            index += 1

            val percText = TextView(activity)
            val tempPerc = (values.netChange / values.lastPrice) * 100
            if (tempPerc >= 0) {
                percText.setTextColor(Color.parseColor("#45DE00"))
            } else {
                percText.setTextColor(Color.parseColor("#FF532F"))
            }
            val roundedPerc = String.format("%.2f", tempPerc) + "%"
            percText.text = roundedPerc
            percText.textSize = 12F
            percText.id = View.generateViewId()
            cLayout.addView(percText, index)
            index += 1

            constraintSet.clear(cLayout.id)
            constraintSet.clone(cLayout)

            constraintSet.connect(
                curValueText.id,
                ConstraintSet.TOP,
                cLayout.id,
                ConstraintSet.TOP,
                30
            )
            constraintSet.connect(
                curValueText.id,
                ConstraintSet.START,
                cLayout.id,
                ConstraintSet.START,
                30
            )

            if (index < 6) {
                constraintSet.connect(
                    tickerText.id,
                    ConstraintSet.TOP,
                    curValueText.id,
                    ConstraintSet.BOTTOM,
                    30
                )
                constraintSet.connect(
                    priceText.id,
                    ConstraintSet.TOP,
                    curValueText.id,
                    ConstraintSet.BOTTOM,
                    30
                )
            } else {
                constraintSet.connect(
                    tickerText.id,
                    ConstraintSet.TOP,
                    lastDesc.id,
                    ConstraintSet.BOTTOM,
                    30
                )
                constraintSet.connect(
                    priceText.id,
                    ConstraintSet.TOP,
                    lastPerc.id,
                    ConstraintSet.BOTTOM,
                    30
                )
            }
            constraintSet.connect(
                tickerText.id,
                ConstraintSet.START,
                cLayout.id,
                ConstraintSet.START,
                30
            )
            constraintSet.connect(
                priceText.id,
                ConstraintSet.END,
                cLayout.id,
                ConstraintSet.END,
                30
            )
            constraintSet.connect(
                descText.id,
                ConstraintSet.TOP,
                tickerText.id,
                ConstraintSet.BOTTOM,
                -5
            )
            constraintSet.connect(
                descText.id,
                ConstraintSet.START,
                cLayout.id,
                ConstraintSet.START,
                30
            )
            constraintSet.connect(
                percText.id,
                ConstraintSet.TOP,
                priceText.id,
                ConstraintSet.BOTTOM,
                -8
            )
            constraintSet.connect(percText.id, ConstraintSet.END, cLayout.id, ConstraintSet.END, 30)

            lastPerc = percText
            lastDesc = descText
            constraintSet.applyTo(cLayout)
            swipeContainer.isRefreshing = false
        }
        val curValueString = "$"+String.format("%.2f", curValue)
        curValueText.text = curValueString
        cLayout.addView(curValueText, index)
    }

}