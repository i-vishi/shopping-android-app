package com.vishalgaur.shoppingapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.vishalgaur.shoppingapp.R
import com.vishalgaur.shoppingapp.databinding.FragmentOrderDetailsBinding
import com.vishalgaur.shoppingapp.viewModels.HomeViewModel

class OrderDetailsFragment : Fragment() {

	private lateinit var binding: FragmentOrderDetailsBinding
	private val viewModel: HomeViewModel by activityViewModels()
	private lateinit var orderId: String

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		binding = FragmentOrderDetailsBinding.inflate(layoutInflater)
		orderId = arguments?.getString("orderId").toString()

		setViews()

		return binding.root
	}

	private fun setViews() {
		binding.orderDetailAppBar.topAppBar.title = getString(R.string.order_details_fragment_title)
		binding.orderDetailAppBar.topAppBar.setNavigationOnClickListener { findNavController().navigateUp() }
		binding.loaderLayout.circularLoader.visibility = View.GONE
		if (viewModel.isUserASeller) {
			binding.orderChangeStatusBtn.visibility = View.VISIBLE
			binding.orderChangeStatusBtn.setOnClickListener {

			}
		} else {
			binding.orderChangeStatusBtn.visibility = View.GONE
		}

	}
}