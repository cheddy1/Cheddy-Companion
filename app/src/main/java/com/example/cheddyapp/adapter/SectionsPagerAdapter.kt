package com.example.cheddyapp.adapter

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.cheddyapp.fragment.BrokerFragment
import com.example.cheddyapp.fragment.RothFragment

private const val NUM_TABS = 2
private const val TAG = "ViewPagerAdapter"
class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return NUM_TABS
    }

    override fun createFragment(position: Int): Fragment {
        Log.i(TAG, "position: $position")
        when (position) {
            0 -> return RothFragment()
            1 -> return BrokerFragment()
        }
        return BrokerFragment()
    }

}
