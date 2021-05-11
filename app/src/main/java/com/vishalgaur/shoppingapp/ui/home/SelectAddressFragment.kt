package com.vishalgaur.shoppingapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.vishalgaur.shoppingapp.R
import com.vishalgaur.shoppingapp.databinding.FragmentSelectAddressBinding

class SelectAddressFragment : Fragment() {

	private lateinit var binding: FragmentSelectAddressBinding

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		binding = FragmentSelectAddressBinding.inflate(layoutInflater)

		setViews()
		return binding.root
	}

	private fun setViews() {
		binding.shipToAppBar.topAppBar.title = getString(R.string.ship_to_title)
		binding.shipToAppBar.topAppBar.inflateMenu(R.menu.menu_with_add_only)
		binding.shipToAppBar.topAppBar.setNavigationOnClickListener {
			findNavController().navigateUp()
		}
		binding.shipToAppBar.topAppBar.setOnMenuItemClickListener { menuItem ->
			if (menuItem.itemId == R.id.add_item) {
				navigateToAddAddress(false)
				true
			} else {
				false
			}
		}

		binding.loaderLayout.circularLoader.visibility = View.GONE
	}

	private fun navigateToAddAddress(isEdit: Boolean) {
		findNavController().navigate(
			R.id.action_selectAddressFragment_to_addEditAddressFragment,
			bundleOf("isEdit" to isEdit)
		)
	}
}