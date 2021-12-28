package com.example.cheddyapp.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.cheddyapp.*
import com.example.cheddyapp.databinding.FragmentRothBinding
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout


class RothFragment : Fragment() {
    private var _binding: FragmentRothBinding? = null
    private val binding get() = _binding!!
    private lateinit var swipeContainer: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRothBinding.inflate(inflater, container, false)
        swipeContainer = binding.rothSwipe
        val stockDataHandler = StockDataHandler("roth", this.activity, binding.rothConstraint, swipeContainer)
        swipeContainer.setOnRefreshListener {
            stockDataHandler.fetchStockData()
        }
        stockDataHandler.fetchStockData()
        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}