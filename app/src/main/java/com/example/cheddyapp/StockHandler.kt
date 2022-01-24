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

class StockDataHandler(
    private val tab: String,
    private val activity: FragmentActivity?,
    private val cLayout: ConstraintLayout,
    private val swipeContainer: SwipeRefreshLayout,
    applicationContext: Context,
) {
    private var index: Int = 0
    val TAG = "StockDataHandler"
    private lateinit var lastPerc: TextView
    private lateinit var lastDesc: TextView
    private lateinit var tickerData: JsonObject
    private lateinit var brokerTickerString: String
    private lateinit var lastOwnedInput: TextView
    private lateinit var lastPercFolioInput: TextView
    private lateinit var lastTickerInput: TextView
    private lateinit var tickerListSQL: List<UserTickerData>
    private val editTextList = mutableListOf<List<EditText>>()

    private val db = Room.databaseBuilder(
        applicationContext,
        AppDatabase::class.java, "cheddy-db"
    ).allowMainThreadQueries().build()

    private val userDataDao = db.userDataDao()

    fun fetchStockData() {
        editTextList.clear()
        val gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create()
        val retrofit = Retrofit.Builder().baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson)).build()
        val stockService = retrofit.create(StockService::class.java)
        val tickerList = mutableListOf<String>()
        tickerListSQL = userDataDao.getData(tab)
        for (ticker in tickerListSQL) {
            tickerList += ticker.ticker
        }
        Log.i("tickerlist: ", tickerList.toString())
        brokerTickerString = tickerList.joinToString(separator = "%2C") { it }
        stockService.getSingleCurPrice("$BASE_URL/quotes?apikey=LEAPZSMYRNJADCF7EJNKI6B09BRHGFHD&symbol=$brokerTickerString")
            .enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    if (response.body().toString() == "{}") {
                        Log.w(TAG, "Did not recieve valid response body")
                        swipeContainer.isRefreshing = false
                        generateEditButton(false)
                        return
                    }
                    tickerData = response.body()!!
                    generateEditButton(true)
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Log.e(TAG, "onFailure $t")
                    swipeContainer.isRefreshing = false
                }

            })
    }

    private fun generateEditButton(valid: Boolean) {
        index = 0
        cLayout.removeAllViews()
        val constraintSet = ConstraintSet()
        val editInputButton = Button(activity)
        editInputButton.text = "Edit Stocks"
        editInputButton.setOnClickListener {
            swipeContainer.isEnabled = false
            displayPopulatedList()
            displaySubmitButton()
        }
        editInputButton.id = View.generateViewId()
        cLayout.addView(editInputButton, index)
        index += 1

        constraintSet.clear(cLayout.id)
        constraintSet.clone(cLayout)

        constraintSet.connect(
            editInputButton.id,
            ConstraintSet.TOP,
            cLayout.id,
            ConstraintSet.TOP,
            30
        )
        constraintSet.connect(
            editInputButton.id,
            ConstraintSet.START,
            cLayout.id,
            ConstraintSet.START,
            30
        )
        constraintSet.connect(
            editInputButton.id,
            ConstraintSet.END,
            cLayout.id,
            ConstraintSet.END,
            30
        )
        constraintSet.applyTo(cLayout)
        if (valid) {
            generateTextViews(editInputButton, constraintSet)
        }

    }

    private fun generateTextViews(editInputButton: Button, constraintSet: ConstraintSet) {
        var curValue = 0.0
        var dailyPM = 0.0

        val curValueText = TextView(activity)
        curValueText.textSize = 20F
        curValueText.setTextColor(Color.parseColor("#FFFFFF"))
        curValueText.id = View.generateViewId()
        cLayout.addView(curValueText, index)
        index += 1

        val curPercText = TextView(activity)
        curPercText.textSize = 14F
        curPercText.id = View.generateViewId()
        cLayout.addView(curPercText, index)
        index += 1

        for (ticker in tickerListSQL) {
            val values =
                Gson().fromJson(tickerData.get(ticker.ticker), StockData::class.java) ?: continue
            curValue += values.lastPrice * ticker.amountOwned

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
            dailyPM += tempPerc * values.lastPrice * ticker.amountOwned
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
                editInputButton.id,
                ConstraintSet.TOP,
                cLayout.id,
                ConstraintSet.TOP,
                30
            )
            constraintSet.connect(
                editInputButton.id,
                ConstraintSet.START,
                cLayout.id,
                ConstraintSet.START,
                30
            )
            constraintSet.connect(
                editInputButton.id,
                ConstraintSet.END,
                cLayout.id,
                ConstraintSet.END,
                30
            )

            constraintSet.connect(
                curValueText.id,
                ConstraintSet.TOP,
                cLayout.id,
                ConstraintSet.TOP,
                30
            )
            constraintSet.connect(
                curValueText.id,
                ConstraintSet.END,
                cLayout.id,
                ConstraintSet.END,
                30
            )
            constraintSet.connect(
                curPercText.id,
                ConstraintSet.TOP,
                curValueText.id,
                ConstraintSet.BOTTOM,
                -10
            )
            constraintSet.connect(
                curPercText.id,
                ConstraintSet.END,
                cLayout.id,
                ConstraintSet.END,
                30
            )

            if (index < 8) {
                constraintSet.connect(
                    tickerText.id,
                    ConstraintSet.TOP,
                    editInputButton.id,
                    ConstraintSet.BOTTOM,
                    30
                )
                constraintSet.connect(
                    priceText.id,
                    ConstraintSet.TOP,
                    editInputButton.id,
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
        val tempCurValue = "$" + String.format("%.2f", curValue)
        curValueText.text = tempCurValue


        val tempPercValue = String.format("%.2f", dailyPM / curValue) + "%"
        if (dailyPM / curValue >= 0) {
            curPercText.setTextColor(Color.parseColor("#45DE00"))
        } else {
            curPercText.setTextColor(Color.parseColor("#FF532F"))
        }
        curPercText.text = tempPercValue
        swipeContainer.isRefreshing = false
    }

    private fun displayPopulatedList(
    ) {
        val tickerListSQL: List<UserTickerData> = userDataDao.getData(tab)
        if (!tickerListSQL.isNullOrEmpty()) {
            index = 0
            cLayout.removeAllViews()

            val viewChangerButton = Button(activity)

            viewChangerButton.text = "View Stocks"
            viewChangerButton.setOnClickListener {
                swipeContainer.isEnabled = true
                generateEditButton(true)
            }
            viewChangerButton.id = View.generateViewId()
            cLayout.addView(viewChangerButton, index)
            index += 1


            val constraintSet = ConstraintSet()
            for (ticker in tickerListSQL) {
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
                ownedInput.inputType =
                    InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
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

                val tempEditTextList: List<EditText> =
                    listOf(tickerInput, ownedInput, percFolioInput)
                editTextList += tempEditTextList

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

                if (index < 5) {
                    constraintSet.connect(
                        tickerInput.id,
                        ConstraintSet.TOP,
                        viewChangerButton.id,
                        ConstraintSet.BOTTOM,
                        20
                    )
                    constraintSet.connect(
                        ownedInput.id,
                        ConstraintSet.TOP,
                        viewChangerButton.id,
                        ConstraintSet.BOTTOM,
                        20
                    )
                    constraintSet.connect(
                        ownedInput.id,
                        ConstraintSet.START,
                        tickerInput.id,
                        ConstraintSet.END,
                        16
                    )
                    constraintSet.connect(
                        percFolioInput.id,
                        ConstraintSet.TOP,
                        viewChangerButton.id,
                        ConstraintSet.BOTTOM,
                        20
                    )
                    constraintSet.connect(
                        percFolioInput.id,
                        ConstraintSet.START,
                        ownedInput.id,
                        ConstraintSet.END,
                        16
                    )

                } else {
                    constraintSet.connect(
                        tickerInput.id,
                        ConstraintSet.TOP,
                        lastTickerInput.id,
                        ConstraintSet.BOTTOM,
                        20
                    )
                    constraintSet.connect(
                        ownedInput.id,
                        ConstraintSet.TOP,
                        lastOwnedInput.id,
                        ConstraintSet.BOTTOM,
                        20
                    )
                    constraintSet.connect(
                        ownedInput.id,
                        ConstraintSet.START,
                        tickerInput.id,
                        ConstraintSet.END,
                        16
                    )
                    constraintSet.connect(
                        percFolioInput.id,
                        ConstraintSet.TOP,
                        lastPercFolioInput.id,
                        ConstraintSet.BOTTOM,
                        20
                    )
                    constraintSet.connect(
                        percFolioInput.id,
                        ConstraintSet.START,
                        ownedInput.id,
                        ConstraintSet.END,
                        16
                    )
                }
                lastTickerInput = tickerInput
                lastOwnedInput = ownedInput
                lastPercFolioInput = percFolioInput
                constraintSet.applyTo(cLayout)
            }

            displayEmptyInputRow(viewChangerButton, editTextList)
        }
    }

    private fun displayEmptyInputRow(
        viewChangerButton: Button,
        editTextList: MutableList<List<EditText>>
    ) {
        val constraintSet = ConstraintSet()
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

        val tempEditTextList: List<EditText> = listOf(tickerInput, ownedInput, percFolioInput)
        editTextList += tempEditTextList

        constraintSet.clear(cLayout.id)
        constraintSet.clone(cLayout)

        val topConstraint: TextView = if (index < 4) {
            viewChangerButton
        } else {
            lastTickerInput
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
            20
        )
        constraintSet.connect(
            ownedInput.id,
            ConstraintSet.TOP,
            topConstraint.id,
            ConstraintSet.BOTTOM,
            20
        )
        constraintSet.connect(
            ownedInput.id,
            ConstraintSet.START,
            tickerInput.id,
            ConstraintSet.END,
            16
        )
        constraintSet.connect(
            percFolioInput.id,
            ConstraintSet.TOP,
            topConstraint.id,
            ConstraintSet.BOTTOM,
            20
        )
        constraintSet.connect(
            percFolioInput.id,
            ConstraintSet.START,
            ownedInput.id,
            ConstraintSet.END,
            16
        )
        lastTickerInput = tickerInput
        lastOwnedInput = ownedInput
        lastPercFolioInput = percFolioInput

        constraintSet.applyTo(cLayout)
    }

    private fun displaySubmitButton() {
        val constraintSet = ConstraintSet()
        val submitButton = Button(activity)
        submitButton.text = "Submit"
        var folioWeight = 0

        submitButton.id = View.generateViewId()
        submitButton.setOnClickListener {

            userDataDao.removeTickers(tab)
            for (textInput in editTextList) {
                Log.i("name", textInput[0].text.toString())
                if (textInput[0].text.toString().isNotBlank() && textInput[1].text.toString()
                        .isNotBlank() && textInput[2].text.toString().isNotBlank()
                ) {
                    userDataDao.insertData(
                        UserTickerData(
                            tab,
                            textInput[0].text.toString(),
                            textInput[1].text.toString().toDouble(),
                            textInput[2].text.toString().toInt()
                        )
                    )
                    folioWeight += textInput[2].text.toString().toInt()
                }
            }
            fetchStockData()
        }
        cLayout.addView(submitButton, index)
        index += 1
        constraintSet.clear(cLayout.id)
        constraintSet.clone(cLayout)
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