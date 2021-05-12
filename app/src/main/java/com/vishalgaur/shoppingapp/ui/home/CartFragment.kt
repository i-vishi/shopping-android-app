package com.vishalgaur.shoppingapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.vishalgaur.shoppingapp.R
import com.vishalgaur.shoppingapp.data.UserData
import com.vishalgaur.shoppingapp.data.utils.StoreDataStatus
import com.vishalgaur.shoppingapp.databinding.FragmentCartBinding
import com.vishalgaur.shoppingapp.viewModels.OrderViewModel

class CartFragment : Fragment() {

	private lateinit var binding: FragmentCartBinding
	private val orderViewModel: OrderViewModel by activityViewModels()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		binding = FragmentCartBinding.inflate(layoutInflater)

		setViews()
		setObservers()

		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		orderViewModel.getCartItems()

		orderViewModel.cartItems.observe(viewLifecycleOwner) { itemList ->
			// set adapter and recyclerview

			if (itemList.isNotEmpty()) {
				setPriceCard(itemList)
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
		orderViewModel.priceList.observe(viewLifecycleOwner) {
			orderViewModel.cartItems.value?.let { it1 -> setPriceCard(it1) }
		}
	}

	private fun setViews() {
		binding.loaderLayout.circularLoader.visibility = View.GONE
		binding.cartAppBar.topAppBar.title = getString(R.string.cart_fragment_label)
		binding.cartCheckOutBtn.setOnClickListener {
			navigateToSelectAddress()
		}
	}

	private fun setPriceCard(itemList: List<UserData.CartItem>) {
		binding.cartPriceCardLayout.priceItemsLabelTv.text =
			getString(R.string.price_card_items_string, itemList.size.toString())
		binding.cartPriceCardLayout.priceItemsAmountTv.text =
			getString(R.string.price_text, orderViewModel.getItemsPriceTotal().toString())
		binding.cartPriceCardLayout.priceShippingAmountTv.text = getString(R.string.price_text, "0")
		binding.cartPriceCardLayout.priceChargesAmountTv.text = getString(R.string.price_text, "0")
		binding.cartPriceCardLayout.priceTotalAmountTv.text =
			getString(R.string.price_text, orderViewModel.getItemsPriceTotal().toString())
	}

	private fun navigateToSelectAddress() {
		findNavController().navigate(R.id.action_cartFragment_to_selectAddressFragment)
	}
}