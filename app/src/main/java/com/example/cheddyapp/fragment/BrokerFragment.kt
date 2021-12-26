package com.example.cheddyapp.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.cheddyapp.*
import com.example.cheddyapp.databinding.FragmentBrokeBinding


class BrokerFragment : Fragment() {
    private var _binding: FragmentBrokeBinding? = null
    private val binding get() = _binding!!
    private lateinit var swipeContainer: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBrokeBinding.inflate(inflater, container, false)
        swipeContainer = requireActivity().findViewById(R.id.swipeContainer)
        val stockDataHandler = StockDataHandler("broker", this.activity, binding.brokerConstraint, swipeContainer)
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