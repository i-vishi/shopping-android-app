package com.vishalgaur.shoppingapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.vishalgaur.shoppingapp.R
import com.vishalgaur.shoppingapp.databinding.FragmentOrdersBinding

class OrdersFragment: Fragment() {

	private lateinit var binding: FragmentOrdersBinding
	private lateinit var ordersAdapter: OrdersAdapter

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		binding = FragmentOrdersBinding.inflate(layoutInflater)

		setViews()
		setObservers()

		return binding.root
	}

	private fun setViews() {
		binding.loaderLayout.circularLoader.visibility = View.GONE
		binding.ordersAppBar.topAppBar.title = getString(R.string.orders_fragment_title)
		binding.ordersEmptyTextView.visibility = View.GONE
	if(context != null) {
		ordersAdapter = OrdersAdapter(emptyList(), requireContext())
		binding.orderAllOrdersRecyclerView.adapter = ordersAdapter
	}
	}

	private fun setObservers() {

	}
}