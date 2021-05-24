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
import com.vishalgaur.shoppingapp.databinding.FragmentOrderDetailsBinding
import com.vishalgaur.shoppingapp.ui.getCompleteAddress
import com.vishalgaur.shoppingapp.viewModels.HomeViewModel
import java.time.Month
import java.util.*

class OrderDetailsFragment : Fragment() {

	private lateinit var binding: FragmentOrderDetailsBinding
	private val viewModel: HomeViewModel by activityViewModels()
	private lateinit var orderId: String
	private lateinit var productsAdapter: OrderProductsAdapter

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		binding = FragmentOrderDetailsBinding.inflate(layoutInflater)
		orderId = arguments?.getString("orderId").toString()
		viewModel.getOrderDetailsByOrderId(orderId)
		setViews()
		setObservers()
		return binding.root
	}

	private fun setViews() {
		binding.orderDetailAppBar.topAppBar.title = getString(R.string.order_details_fragment_title)
		binding.orderDetailAppBar.topAppBar.setNavigationOnClickListener { findNavController().navigateUp() }
		binding.loaderLayout.circularLoader.visibility = View.GONE
		binding.orderDetailsConstraintGroup.visibility = View.GONE

		if (context != null) {
			setProductsAdapter(viewModel.selectedOrder.value?.items)
			binding.orderDetailsProRecyclerView.adapter = productsAdapter
		}
	}

	private fun setObservers() {
		viewModel.storeDataStatus.observe(viewLifecycleOwner) { status ->
			when (status) {
				StoreDataStatus.LOADING -> {
					binding.loaderLayout.circularLoader.visibility = View.VISIBLE
					binding.loaderLayout.circularLoader.showAnimationBehavior
					binding.orderDetailsConstraintGroup.visibility = View.GONE
				}
				else -> {
					binding.loaderLayout.circularLoader.hideAnimationBehavior
					binding.loaderLayout.circularLoader.visibility = View.GONE
				}
			}
		}
		viewModel.selectedOrder.observe(viewLifecycleOwner) { orderData ->
			if (orderData != null) {
				binding.orderDetailsConstraintGroup.visibility = View.VISIBLE
				setAllViews(orderData)
				val items = orderData.items
				val likeList = viewModel.userLikes.value ?: emptyList()
				val prosList = viewModel.orderProducts.value ?: emptyList()
				productsAdapter.apply {
					data = items
					proList = prosList
					likesList = likeList
				}
				binding.orderDetailsProRecyclerView.adapter = productsAdapter
				binding.orderDetailsProRecyclerView.adapter?.notifyDataSetChanged()
			}
		}
	}

	private fun setAllViews(orderData: UserData.OrderItem) {
		if (viewModel.isUserASeller) {
			binding.orderChangeStatusBtn.visibility = View.VISIBLE
			binding.orderChangeStatusBtn.setOnClickListener {

			}
		} else {
			binding.orderChangeStatusBtn.visibility = View.GONE
		}
		val calendar = Calendar.getInstance()
		calendar.time = orderData.orderDate
		binding.orderDetailsShippingAddLayout.shipDateValueTv.text = getString(
			R.string.order_date_text,
			Month.values()[(calendar.get(Calendar.MONTH))].name,
			calendar.get(Calendar.DAY_OF_MONTH).toString(),
			calendar.get(Calendar.YEAR).toString()
		)
		binding.orderDetailsShippingAddLayout.shipAddValueTv.text =
			getCompleteAddress(orderData.deliveryAddress)
		binding.orderDetailsShippingAddLayout.shipCurrStatusValueTv.text = orderData.status

		setPriceCard(orderData)
	}

	private fun setPriceCard(orderData: UserData.OrderItem) {
		binding.orderDetailsPaymentLayout.priceItemsLabelTv.text = getString(
			R.string.price_card_items_string,
			getItemsCount(orderData.items).toString()
		)
		val itemsPriceTotal = getItemsPriceTotal(orderData.itemsPrices, orderData.items)
		binding.orderDetailsPaymentLayout.priceItemsAmountTv.text =
			getString(
				R.string.price_text,
				itemsPriceTotal.toString()
			)
		binding.orderDetailsPaymentLayout.priceShippingAmountTv.text =
			getString(R.string.price_text, "0")
		binding.orderDetailsPaymentLayout.priceChargesAmountTv.text =
			getString(R.string.price_text, "0")
		binding.orderDetailsPaymentLayout.priceTotalAmountTv.text =
			getString(R.string.price_text, (itemsPriceTotal + orderData.shippingCharges).toString())
	}

	private fun setProductsAdapter(itemsList: List<UserData.CartItem>?) {
		val items = itemsList ?: emptyList()
		val likesList = viewModel.userLikes.value ?: emptyList()
		val proList = viewModel.orderProducts.value ?: emptyList()
		productsAdapter = OrderProductsAdapter(requireContext(), items, proList, likesList)
	}

	private fun getItemsCount(cartItems: List<UserData.CartItem>): Int {
		var totalCount = 0
		cartItems.forEach {
			totalCount += it.quantity
		}
		return totalCount
	}

	private fun getItemsPriceTotal(
		priceList: Map<String, Double>,
		cartItems: List<UserData.CartItem>
	): Double {
		var totalPrice = 0.0
		priceList.forEach { (itemId, price) ->
			totalPrice += price * (cartItems.find { it.itemId == itemId }?.quantity ?: 1)
		}
		return totalPrice
	}
}