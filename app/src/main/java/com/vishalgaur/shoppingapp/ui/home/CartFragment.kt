package com.vishalgaur.shoppingapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.vishalgaur.shoppingapp.R
import com.vishalgaur.shoppingapp.databinding.FragmentCartBinding

class CartFragment: Fragment() {
	private lateinit var binding: FragmentCartBinding

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		binding = FragmentCartBinding.inflate(layoutInflater)

		setViews()
		return binding.root
	}

	private fun setViews() {
		binding.loaderLayout.circularLoader.visibility = View.GONE
		binding.cartAppBar.topAppBar.title = getString(R.string.cart_fragment_label)
		binding.cartCheckOutBtn.setOnClickListener {
			navigateToSelectAddress()
		}
	}

	private fun navigateToSelectAddress() {
		findNavController().navigate(R.id.action_cartFragment_to_selectAddressFragment)
	}
}