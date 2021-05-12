package com.vishalgaur.shoppingapp.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vishalgaur.shoppingapp.R
import com.vishalgaur.shoppingapp.data.UserData
import com.vishalgaur.shoppingapp.data.utils.StoreDataStatus
import com.vishalgaur.shoppingapp.databinding.FragmentCartBinding
import com.vishalgaur.shoppingapp.databinding.LayoutPriceCardBinding
import com.vishalgaur.shoppingapp.viewModels.OrderViewModel

private const val TAG = "CartFragment"

class CartFragment : Fragment() {

	private lateinit var binding: FragmentCartBinding
	private val orderViewModel: OrderViewModel by activityViewModels()
	private lateinit var itemsAdapter: CartItemAdapter

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
			if (context != null) {
				if (itemList.isNotEmpty()) {
					setItemsAdapter(itemList)
					val concatAdapter = ConcatAdapter(
						itemsAdapter,
						PriceCardAdapter()
					)
					binding.cartProductsRecyclerView.adapter = concatAdapter
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
					binding.cartProductsRecyclerView.visibility = View.GONE
				}
				else -> {
					binding.cartProductsRecyclerView.visibility = View.VISIBLE
					binding.loaderLayout.circularLoader.hideAnimationBehavior
					binding.loaderLayout.circularLoader.visibility = View.GONE
				}
			}
		}
		orderViewModel.priceList.observe(viewLifecycleOwner) {
			orderViewModel.cartItems.value?.let {
				setItemsAdapter(it)
				val concatAdapter = ConcatAdapter(
					itemsAdapter,
					PriceCardAdapter()
				)
				binding.cartProductsRecyclerView.adapter = concatAdapter
			}
		}
	}

	private fun setViews() {
		binding.loaderLayout.circularLoader.visibility = View.GONE
		binding.cartAppBar.topAppBar.title = getString(R.string.cart_fragment_label)
		binding.cartCheckOutBtn.setOnClickListener {
			navigateToSelectAddress()
		}
	}

	private fun setItemsAdapter(itemList: List<UserData.CartItem>?) {
		val items = itemList ?: emptyList()
		val likesList = emptyList<String>()
		val proList = orderViewModel.cartProducts.value ?: emptyList()
		itemsAdapter = CartItemAdapter(requireContext(), items, proList, likesList)
		itemsAdapter.onClickListener = object : CartItemAdapter.OnClickListener {
			override fun onLikeClick(productId: String) {
				Log.d(TAG, "onToggle Like Clicked")
				orderViewModel.toggleLikeProduct(productId)
			}

			override fun onDeleteClick(itemId: String) {
				Log.d(TAG, "onDelete: initiated")
			}

			override fun onPlusClick() {
				Log.d(TAG, "onPlus: Increasing quantity")
			}

			override fun onMinusClick() {
				Log.d(TAG, "onMinus: decreasing quantity")
			}
		}
	}

//	private fun setPriceCard(itemList: List<UserData.CartItem>) {
//		binding.cartPriceCardLayout.priceItemsLabelTv.text =
//			getString(R.string.price_card_items_string, itemList.size.toString())
//		binding.cartPriceCardLayout.priceItemsAmountTv.text =
//			getString(R.string.price_text, orderViewModel.getItemsPriceTotal().toString())
//		binding.cartPriceCardLayout.priceShippingAmountTv.text = getString(R.string.price_text, "0")
//		binding.cartPriceCardLayout.priceChargesAmountTv.text = getString(R.string.price_text, "0")
//		binding.cartPriceCardLayout.priceTotalAmountTv.text =
//			getString(R.string.price_text, orderViewModel.getItemsPriceTotal().toString())
//	}

	private fun navigateToSelectAddress() {
		findNavController().navigate(R.id.action_cartFragment_to_selectAddressFragment)
	}

	inner class PriceCardAdapter : RecyclerView.Adapter<PriceCardAdapter.ViewHolder>() {

		inner class ViewHolder(private val priceCardBinding: LayoutPriceCardBinding) :
			RecyclerView.ViewHolder(priceCardBinding.root) {
			fun bind() {
				priceCardBinding.priceItemsLabelTv.text = getString(
					R.string.price_card_items_string,
					orderViewModel.getItemsCount().toString()
				)
				priceCardBinding.priceItemsAmountTv.text =
					getString(R.string.price_text, orderViewModel.getItemsPriceTotal().toString())
				priceCardBinding.priceShippingAmountTv.text = getString(R.string.price_text, "0")
				priceCardBinding.priceChargesAmountTv.text = getString(R.string.price_text, "0")
				priceCardBinding.priceTotalAmountTv.text =
					getString(R.string.price_text, orderViewModel.getItemsPriceTotal().toString())
			}
		}

		override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
			return ViewHolder(
				LayoutPriceCardBinding.inflate(
					LayoutInflater.from(parent.context),
					parent,
					false
				)
			)
		}

		override fun onBindViewHolder(holder: ViewHolder, position: Int) {
			holder.bind()
		}

		override fun getItemCount() = 1
	}
}