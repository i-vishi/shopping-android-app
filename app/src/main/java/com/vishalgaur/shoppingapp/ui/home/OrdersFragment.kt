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
import com.vishalgaur.shoppingapp.databinding.FragmentOrdersBinding
import com.vishalgaur.shoppingapp.viewModels.HomeViewModel

private const val TAG = "OrdersFragment"

class OrdersFragment : Fragment() {

	private lateinit var binding: FragmentOrdersBinding
	private lateinit var ordersAdapter: OrdersAdapter
	private val viewModel: HomeViewModel by activityViewModels()

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

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		viewModel.getAllOrders()
	}

	private fun setViews() {
		binding.loaderLayout.loaderFrameLayout.visibility = View.GONE
		binding.ordersAppBar.topAppBar.title = getString(R.string.orders_fragment_title)
		binding.ordersAppBar.topAppBar.setNavigationOnClickListener {
			findNavController().navigateUp()
		}
		binding.ordersEmptyTextView.visibility = View.GONE
		if (context != null) {
			ordersAdapter = OrdersAdapter(emptyList(), requireContext())
			ordersAdapter.onClickListener = object : OrdersAdapter.OnClickListener {
				override fun onCardClick(orderId: String) {
					Log.d(TAG, "onOrderSummaryClick: Getting order details")
					findNavController().navigate(
						R.id.action_ordersFragment_to_orderDetailsFragment,
						bundleOf("orderId" to orderId)
					)
				}
			}
			binding.orderAllOrdersRecyclerView.adapter = ordersAdapter
		}
	}

	private fun setObservers() {
		viewModel.storeDataStatus.observe(viewLifecycleOwner) { status ->
			when (status) {
				StoreDataStatus.LOADING -> {
					binding.orderAllOrdersRecyclerView.visibility = View.GONE
					binding.ordersEmptyTextView.visibility = View.GONE
					binding.loaderLayout.loaderFrameLayout.visibility = View.VISIBLE
					binding.loaderLayout.circularLoader.showAnimationBehavior
				}
				else -> {
					binding.loaderLayout.circularLoader.hideAnimationBehavior
					binding.loaderLayout.loaderFrameLayout.visibility = View.GONE
				}
			}

			if (status != null && status != StoreDataStatus.LOADING) {
				viewModel.userOrders.observe(viewLifecycleOwner) { orders ->
					if (orders.isNotEmpty()) {
						ordersAdapter.data = orders.sortedByDescending { it.orderDate }
						binding.orderAllOrdersRecyclerView.adapter?.notifyDataSetChanged()
						binding.orderAllOrdersRecyclerView.visibility = View.VISIBLE
					} else if (orders.isEmpty()) {
						binding.loaderLayout.circularLoader.hideAnimationBehavior
						binding.loaderLayout.loaderFrameLayout.visibility = View.GONE
						binding.ordersEmptyTextView.visibility = View.VISIBLE
					}
				}
			}
		}
	}
}