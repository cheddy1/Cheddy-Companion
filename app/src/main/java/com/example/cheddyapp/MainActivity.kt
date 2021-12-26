package com.example.cheddyapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.cheddyapp.adapter.ViewPagerAdapter
import com.example.cheddyapp.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator



private val tabArray = arrayOf(
    "Roth IRA",
    "Brokerage"
)



class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewPager = binding.viewPager
        val tabLayout = binding.tabLayout



        val adapter = ViewPagerAdapter(supportFragmentManager, lifecycle)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabArray[position]
        }.attach()

//        swipeContainer = findViewById(R.id.swipeContainer)
//        swipeContainer.setOnRefreshListener {
//            val cLayout = findViewById<ConstraintLayout>(R.id.rothConstraint)
//            cLayout.removeAllViews()
//        }
//        swipeContainer.isRefreshing = false

    }




}





