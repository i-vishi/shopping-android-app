package com.vishalgaur.shoppingapp.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.vishalgaur.shoppingapp.R
import com.vishalgaur.shoppingapp.data.utils.StoreDataStatus
import com.vishalgaur.shoppingapp.databinding.FragmentSelectAddressBinding
import com.vishalgaur.shoppingapp.viewModels.OrderViewModel

private const val TAG = "ShipToFragment"

class SelectAddressFragment : Fragment() {

	private lateinit var binding: FragmentSelectAddressBinding
	private val orderViewModel: OrderViewModel by activityViewModels()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		binding = FragmentSelectAddressBinding.inflate(layoutInflater)

		setViews()
		setObservers()

		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		orderViewModel.getUserAddresses()

		orderViewModel.userAddresses.observe(viewLifecycleOwner) { addressList ->
			if (context != null) {
				val addressAdapter = AddressAdapter(requireContext(), addressList ?: emptyList())
				addressAdapter.onClickListener = object : AddressAdapter.OnClickListener {
					override fun onEditClick(addressTd: String) {
						Log.d(TAG, "onEditAddress: initiated")
					}

					override fun onDeleteClick(addressId: String) {
						Log.d(TAG, "onDeleteAddress: initiated")
					}
				}

				binding.shipToAddressesRecyclerView.apply {
					adapter = addressAdapter
				}
			}
		}
	}

	private fun setObservers() {
		orderViewModel.dataStatus.observe(viewLifecycleOwner) { status ->
			when (status) {
				StoreDataStatus.LOADING -> {
					binding.loaderLayout.circularLoader.visibility = View.VISIBLE
					binding.loaderLayout.circularLoader.showAnimationBehavior
				}
				else -> {
					binding.loaderLayout.circularLoader.hideAnimationBehavior
					binding.loaderLayout.circularLoader.visibility = View.GONE
				}
			}
		}
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