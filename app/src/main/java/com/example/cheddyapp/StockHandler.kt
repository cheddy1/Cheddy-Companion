package com.example.cheddyapp

import android.content.Context
import android.graphics.Color
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.FragmentActivity
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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
val brokerTickerList = arrayOf(
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
val rothTickerList = arrayOf(
    "VTI",
    "VXUS",
    "VIOV",
    "QQQM",
    "QQQJ"
)

class StockDataHandler(
    private val tab: String,
    private val activity: FragmentActivity?,
    private val cLayout: ConstraintLayout,
    private val swipeContainer: SwipeRefreshLayout,
    applicationContext: Context,
) {
    private var index: Int = 0
    private var curValue: Double = 0.0
    val TAG = "StockDataHandler"
    private lateinit var lastPerc: TextView
    private lateinit var lastDesc: TextView
    private lateinit var tickerData: JsonObject
    private lateinit var brokerTickerString: String
    private lateinit var lastOwnedInput: TextView
    private lateinit var lastPercFolioInput: TextView
    private lateinit var lastTickerInput: TextView
    private var amountOwned: Double = 0.0

    private val db = Room.databaseBuilder(
        applicationContext,
        AppDatabase::class.java, "cheddy-db"
    ).allowMainThreadQueries().build()

    private val userDataDao = db.userDataDao()
    private var tickerListSQL: List<UserTickerData> = userDataDao.getData(tab)

    fun fetchStockData() {
        val gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create()
        val retrofit = Retrofit.Builder().baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson)).build()
        val stockService = retrofit.create(StockService::class.java)
        val tickerList = mutableListOf<String>()
        for (ticker in tickerListSQL){
            tickerList += ticker.ticker
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
                    generateTextViews(tickerListSQL)
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Log.e(TAG, "onFailure $t")
                }

            })
    }

    private fun generateTextViews(tickerListSQL: List<UserTickerData>) {
        index = 0
        curValue = 0.0
        cLayout.removeAllViews()
        val constraintSet = ConstraintSet()

        val button = Button(activity)
        button.text = "Edit Stocks"
        button.setOnClickListener(View.OnClickListener {
            swipeContainer.isEnabled = false
            generateInputFields()
        })
        button.id = View.generateViewId()
        cLayout.addView(button, index)

        val curValueText = TextView(activity)
        curValueText.textSize = 24F
        curValueText.setTextColor(Color.parseColor("#FFFFFF"))
        curValueText.id = View.generateViewId()


        for (ticker in tickerListSQL) {

            val values = Gson().fromJson(tickerData.get(ticker.ticker), StockData::class.java)
            curValue += values.lastPrice * amountOwned

            val tickerText = TextView(activity)
            tickerText.text = ticker.ticker
            tickerText.setTextColor(Color.parseColor("#FFFFFF"))
            tickerText.textSize = 28F
            tickerText.id = View.generateViewId()
            cLayout.addView(tickerText, index)
            index += 1

            val priceText = TextView(activity)
            val tempPrice = "$" + String.format("%.2f", values.lastPrice)
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
                button.id,
                ConstraintSet.TOP,
                cLayout.id,
                ConstraintSet.TOP,
                30
            )
            constraintSet.connect(
                button.id,
                ConstraintSet.START,
                cLayout.id,
                ConstraintSet.START,
                30
            )
            constraintSet.connect(
                button.id,
                ConstraintSet.END,
                cLayout.id,
                ConstraintSet.END,
                30
            )

            if (index < 6) {
                constraintSet.connect(
                    tickerText.id,
                    ConstraintSet.TOP,
                    button.id,
                    ConstraintSet.BOTTOM,
                    30
                )
                constraintSet.connect(
                    priceText.id,
                    ConstraintSet.TOP,
                    button.id,
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
        }
        swipeContainer.isRefreshing = false
    }

    private fun generateInputFields() {
        index = 0
        cLayout.removeAllViews()
        val viewChangerButton = Button(activity)

        viewChangerButton.text = "View Stocks"
        viewChangerButton.setOnClickListener(View.OnClickListener {
            swipeContainer.isEnabled = true
            generateTextViews(tickerListSQL)
        })
        viewChangerButton.id = View.generateViewId()
        cLayout.addView(viewChangerButton, index)

        val submitButton = Button(activity)
        submitButton.text = "Submit"

        submitButton.id = View.generateViewId()
        cLayout.addView(submitButton, index)

        val tickerListSQL: List<UserTickerData> = userDataDao.getData(tab)
        if (tickerListSQL.isNullOrEmpty()) {
            displayEmptyInputRow(viewChangerButton, submitButton)
        } else {
            displayPopulatedList(tickerListSQL, viewChangerButton, submitButton)
        }

    }

    private fun displayPopulatedList(sqlTickerList: List<UserTickerData>, viewChangerButton: Button, submitButton: Button) {
        for (ticker in sqlTickerList) {
            val constraintSet = ConstraintSet()

            val tickerInput = EditText(activity)
            tickerInput.hint = "Ticker"
            tickerInput.setText(ticker.ticker)
            tickerInput.width = 200
            tickerInput.height = 60
            tickerInput.inputType = InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
            tickerInput.id = View.generateViewId()
            cLayout.addView(tickerInput, index)
            index += 1

            val ownedInput = EditText(activity)
            ownedInput.hint = "# Owned"
            ownedInput.setText(ticker.amountOwned.toString())
            ownedInput.width = 240
            ownedInput.height = 60
            ownedInput.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            ownedInput.id = View.generateViewId()
            cLayout.addView(ownedInput, index)
            index += 1

            val percFolioInput = EditText(activity)
            percFolioInput.hint = "% Weight"
            percFolioInput.setText(ticker.folioWeight.toString())
            percFolioInput.width = 245
            percFolioInput.height = 60
            percFolioInput.inputType = InputType.TYPE_CLASS_NUMBER
            percFolioInput.id = View.generateViewId()
            cLayout.addView(percFolioInput, index)
            index += 1


            constraintSet.clear(cLayout.id)
            constraintSet.clone(cLayout)

            constraintSet.connect(
                viewChangerButton.id,
                ConstraintSet.TOP,
                cLayout.id,
                ConstraintSet.TOP,
                30
            )
            constraintSet.connect(
                viewChangerButton.id,
                ConstraintSet.START,
                cLayout.id,
                ConstraintSet.START,
                30
            )
            constraintSet.connect(
                viewChangerButton.id,
                ConstraintSet.END,
                cLayout.id,
                ConstraintSet.END,
                30
            )

            if (index < 4) {
                constraintSet.connect(
                    tickerInput.id,
                    ConstraintSet.TOP,
                    viewChangerButton.id,
                    ConstraintSet.BOTTOM,
                    30
                )
                constraintSet.connect(
                    ownedInput.id,
                    ConstraintSet.TOP,
                    viewChangerButton.id,
                    ConstraintSet.BOTTOM,
                    30
                )
                constraintSet.connect(
                    ownedInput.id,
                    ConstraintSet.START,
                    tickerInput.id,
                    ConstraintSet.END,
                    12
                )
                constraintSet.connect(
                    percFolioInput.id,
                    ConstraintSet.TOP,
                    viewChangerButton.id,
                    ConstraintSet.BOTTOM,
                    30
                )
                constraintSet.connect(
                    percFolioInput.id,
                    ConstraintSet.START,
                    ownedInput.id,
                    ConstraintSet.END,
                    12
                )

            } else {
                constraintSet.connect(
                    tickerInput.id,
                    ConstraintSet.TOP,
                    lastTickerInput.id,
                    ConstraintSet.BOTTOM,
                    30
                )
                constraintSet.connect(
                    ownedInput.id,
                    ConstraintSet.TOP,
                    lastOwnedInput.id,
                    ConstraintSet.BOTTOM,
                    30
                )
                constraintSet.connect(
                    ownedInput.id,
                    ConstraintSet.START,
                    tickerInput.id,
                    ConstraintSet.END,
                    12
                )
                constraintSet.connect(
                    percFolioInput.id,
                    ConstraintSet.TOP,
                    lastPercFolioInput.id,
                    ConstraintSet.BOTTOM,
                    30
                )
                constraintSet.connect(
                    percFolioInput.id,
                    ConstraintSet.START,
                    ownedInput.id,
                    ConstraintSet.END,
                    12
                )
            }
            lastTickerInput = tickerInput
            lastOwnedInput = ownedInput
            lastPercFolioInput = percFolioInput

            constraintSet.connect(
                submitButton.id,
                ConstraintSet.TOP,
                lastTickerInput.id,
                ConstraintSet.BOTTOM,
                30
            )
            constraintSet.connect(
                submitButton.id,
                ConstraintSet.START,
                cLayout.id,
                ConstraintSet.START,
                30
            )
            constraintSet.connect(
                submitButton.id,
                ConstraintSet.END,
                cLayout.id,
                ConstraintSet.END,
                30
            )

            constraintSet.applyTo(cLayout)
        }
    }

    private fun displayEmptyInputRow(viewChangerButton: Button, submitButton: Button) {
        val constraintSet = ConstraintSet()
        lateinit var topConstraint: TextView

        val tickerInput = EditText(activity)
        tickerInput.hint = "Ticker"
        tickerInput.width = 200
        tickerInput.height = 60
        tickerInput.inputType = InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
        tickerInput.id = View.generateViewId()
        cLayout.addView(tickerInput, index)
        index += 1

        val ownedInput = EditText(activity)
        ownedInput.hint = "# Owned"
        ownedInput.width = 240
        ownedInput.height = 60
        ownedInput.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        ownedInput.id = View.generateViewId()
        cLayout.addView(ownedInput, index)
        index += 1

        val percFolioInput = EditText(activity)
        percFolioInput.hint = "% Weight"
        percFolioInput.width = 245
        percFolioInput.height = 60
        percFolioInput.inputType = InputType.TYPE_CLASS_NUMBER
        percFolioInput.id = View.generateViewId()
        cLayout.addView(percFolioInput, index)
        index += 1

        submitButton.setOnClickListener(View.OnClickListener {
            if (tickerInput.text.toString().isNotBlank() && ownedInput.text.toString().isNotBlank() && percFolioInput.text.toString().isNotBlank()) {
                userDataDao.insertData(UserTickerData(tab, tickerInput.text.toString(), 5.0, 100))
            }
        })

        constraintSet.clear(cLayout.id)
        constraintSet.clone(cLayout)
        if (index < 4){
            topConstraint = viewChangerButton
        }
        else{
            topConstraint = lastTickerInput
        }
        constraintSet.connect(
            viewChangerButton.id,
            ConstraintSet.TOP,
            cLayout.id,
            ConstraintSet.TOP,
            30
        )
        constraintSet.connect(
            viewChangerButton.id,
            ConstraintSet.START,
            cLayout.id,
            ConstraintSet.START,
            30
        )
        constraintSet.connect(
            viewChangerButton.id,
            ConstraintSet.END,
            cLayout.id,
            ConstraintSet.END,
            30
        )

        constraintSet.connect(
            tickerInput.id,
            ConstraintSet.TOP,
            topConstraint.id,
            ConstraintSet.BOTTOM,
            30
        )
        constraintSet.connect(
            ownedInput.id,
            ConstraintSet.TOP,
            topConstraint.id,
            ConstraintSet.BOTTOM,
            30
        )
        constraintSet.connect(
            ownedInput.id,
            ConstraintSet.START,
            tickerInput.id,
            ConstraintSet.END,
            12
        )
        constraintSet.connect(
            percFolioInput.id,
            ConstraintSet.TOP,
            topConstraint.id,
            ConstraintSet.BOTTOM,
            30
        )
        constraintSet.connect(
            percFolioInput.id,
            ConstraintSet.START,
            ownedInput.id,
            ConstraintSet.END,
            12
        )
        lastTickerInput = tickerInput
        lastOwnedInput = ownedInput
        lastPercFolioInput = percFolioInput

        constraintSet.connect(
            submitButton.id,
            ConstraintSet.TOP,
            lastTickerInput.id,
            ConstraintSet.BOTTOM,
            30
        )
        constraintSet.connect(
            submitButton.id,
            ConstraintSet.START,
            cLayout.id,
            ConstraintSet.START,
            30
        )
        constraintSet.connect(
            submitButton.id,
            ConstraintSet.END,
            cLayout.id,
            ConstraintSet.END,
            30
        )

        constraintSet.applyTo(cLayout)


    }
}

@Database(entities = [UserTickerData::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDataDao(): UserDataDao
}