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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vishalgaur.shoppingapp.R
import com.vishalgaur.shoppingapp.data.UserData
import com.vishalgaur.shoppingapp.data.utils.StoreDataStatus
import com.vishalgaur.shoppingapp.databinding.FragmentCartBinding
import com.vishalgaur.shoppingapp.databinding.LayoutCircularLoaderBinding
import com.vishalgaur.shoppingapp.databinding.LayoutPriceCardBinding
import com.vishalgaur.shoppingapp.viewModels.OrderViewModel

private const val TAG = "CartFragment"

class CartFragment : Fragment() {

	private lateinit var binding: FragmentCartBinding
	private val orderViewModel: OrderViewModel by activityViewModels()
	private lateinit var itemsAdapter: CartItemAdapter
	private lateinit var concatAdapter: ConcatAdapter

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
		orderViewModel.getUserLikes()
		orderViewModel.getCartItems()
	}

	private fun setViews() {
		binding.loaderLayout.loaderFrameLayout.visibility = View.VISIBLE
		binding.loaderLayout.circularLoader.showAnimationBehavior
		binding.cartAppBar.topAppBar.title = getString(R.string.cart_fragment_label)
		binding.cartEmptyTextView.visibility = View.GONE
		binding.cartCheckOutBtn.setOnClickListener {
			navigateToSelectAddress()
		}
		if (context != null) {
			setItemsAdapter(orderViewModel.cartItems.value)
			concatAdapter = ConcatAdapter(itemsAdapter, PriceCardAdapter())
			binding.cartProductsRecyclerView.adapter = concatAdapter
		}
	}

	private fun setObservers() {
		orderViewModel.dataStatus.observe(viewLifecycleOwner) { status ->
			when (status) {
				StoreDataStatus.LOADING -> {
					binding.cartProductsRecyclerView.visibility = View.GONE
					binding.cartCheckOutBtn.visibility = View.GONE
					binding.cartEmptyTextView.visibility = View.GONE
					binding.loaderLayout.loaderFrameLayout.visibility = View.VISIBLE
					binding.loaderLayout.circularLoader.showAnimationBehavior
				}
				else -> {
					binding.loaderLayout.circularLoader.hideAnimationBehavior
					binding.loaderLayout.loaderFrameLayout.visibility = View.GONE
				}
			}
		}
		orderViewModel.dataStatus.observe(viewLifecycleOwner) { status ->
			if (status != null && status != StoreDataStatus.LOADING) {
				orderViewModel.cartProducts.observe(viewLifecycleOwner) { itemList ->
					if (itemList.isNotEmpty()) {
						updateAdapter()
						binding.cartProductsRecyclerView.visibility = View.VISIBLE
						binding.cartCheckOutBtn.visibility = View.VISIBLE
					} else if (itemList.isEmpty()) {
						binding.loaderLayout.loaderFrameLayout.visibility = View.GONE
						binding.loaderLayout.circularLoader.hideAnimationBehavior
						binding.cartEmptyTextView.visibility = View.VISIBLE
					}
				}
			}
		}
		orderViewModel.cartItems.observe(viewLifecycleOwner) { items ->
			if (items.isNotEmpty()) {
				updateAdapter()
			}
		}
		orderViewModel.priceList.observe(viewLifecycleOwner) {
			if (it.isNotEmpty()) {
				updateAdapter()
			}
		}
		orderViewModel.userLikes.observe(viewLifecycleOwner) {
			if (it.isNotEmpty()) {
				updateAdapter()
			}
		}
	}

	private fun updateAdapter() {
		val items = orderViewModel.cartItems.value ?: emptyList()
		val likeList = orderViewModel.userLikes.value ?: emptyList()
		val prosList = orderViewModel.cartProducts.value ?: emptyList()
		itemsAdapter.apply {
			data = items
			proList = prosList
			likesList = likeList
		}
		concatAdapter = ConcatAdapter(itemsAdapter, PriceCardAdapter())
		binding.cartProductsRecyclerView.adapter = concatAdapter
		binding.cartProductsRecyclerView.adapter?.notifyDataSetChanged()
	}

	private fun setItemsAdapter(itemList: List<UserData.CartItem>?) {
		val items = itemList ?: emptyList()
		val likesList = orderViewModel.userLikes.value ?: emptyList()
		val proList = orderViewModel.cartProducts.value ?: emptyList()
		itemsAdapter = CartItemAdapter(requireContext(), items, proList, likesList)
		itemsAdapter.onClickListener = object : CartItemAdapter.OnClickListener {
			override fun onLikeClick(productId: String) {
				Log.d(TAG, "onToggle Like Clicked")
				orderViewModel.toggleLikeProduct(productId)
			}

			override fun onDeleteClick(itemId: String, itemBinding: LayoutCircularLoaderBinding) {
				Log.d(TAG, "onDelete: initiated")
				showDeleteDialog(itemId, itemBinding)
			}

			override fun onPlusClick(itemId: String) {
				Log.d(TAG, "onPlus: Increasing quantity")
				orderViewModel.setQuantityOfItem(itemId, 1)
			}

			override fun onMinusClick(itemId: String, currQuantity: Int,itemBinding: LayoutCircularLoaderBinding) {
				Log.d(TAG, "onMinus: decreasing quantity")
				if (currQuantity == 1) {
					showDeleteDialog(itemId, itemBinding)
				} else {
					orderViewModel.setQuantityOfItem(itemId, -1)
				}
			}
		}
	}

	private fun navigateToSelectAddress() {
		findNavController().navigate(R.id.action_cartFragment_to_selectAddressFragment)
	}

	private fun showDeleteDialog(itemId: String, itemBinding: LayoutCircularLoaderBinding) {
		context?.let {
			MaterialAlertDialogBuilder(it)
				.setTitle(getString(R.string.delete_dialog_title_text))
				.setMessage(getString(R.string.delete_cart_item_message_text))
				.setNegativeButton(getString(R.string.pro_cat_dialog_cancel_btn)) { dialog, _ ->
					dialog.cancel()
					itemBinding.loaderFrameLayout.visibility = View.GONE
				}
				.setPositiveButton(getString(R.string.delete_dialog_delete_btn_text)) { dialog, _ ->
					orderViewModel.deleteItemFromCart(itemId)
					dialog.cancel()
				}.setOnCancelListener {
					itemBinding.loaderFrameLayout.visibility = View.GONE
				}
				.show()
		}
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