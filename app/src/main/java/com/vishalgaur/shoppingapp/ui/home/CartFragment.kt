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

		orderViewModel.cartProducts.observe(viewLifecycleOwner) { itemList ->
			if (context != null) {
				if (itemList.isNotEmpty()) {
					val cartItems = orderViewModel.cartItems.value
					setItemsAdapter(cartItems)
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
					binding.cartCheckOutBtn.isEnabled = false
				}
				else -> {
					binding.cartProductsRecyclerView.visibility = View.VISIBLE
					binding.cartCheckOutBtn.isEnabled = true
					binding.loaderLayout.circularLoader.hideAnimationBehavior
					binding.loaderLayout.circularLoader.visibility = View.GONE
				}
			}
		}
		orderViewModel.cartItems.observe(viewLifecycleOwner) { items ->
			setItemsAdapter(items)
			val concatAdapter = ConcatAdapter(
				itemsAdapter,
				PriceCardAdapter()
			)
			binding.cartProductsRecyclerView.adapter = concatAdapter
		}
		orderViewModel.priceList.observe(viewLifecycleOwner) {
			if (it.isNotEmpty()) {
				orderViewModel.cartItems.value?.let { items ->
					setItemsAdapter(items)
					val concatAdapter = ConcatAdapter(
						itemsAdapter,
						PriceCardAdapter()
					)
					binding.cartProductsRecyclerView.adapter = concatAdapter
				}
			}
		}
		orderViewModel.userLikes.observe(viewLifecycleOwner) {
			orderViewModel.cartItems.value?.let { items ->
				setItemsAdapter(items)
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
		val likesList = orderViewModel.userLikes.value ?: emptyList()
		val proList = orderViewModel.cartProducts.value ?: emptyList()
		itemsAdapter = CartItemAdapter(requireContext(), items, proList, likesList)
		itemsAdapter.onClickListener = object : CartItemAdapter.OnClickListener {
			override fun onLikeClick(productId: String) {
				Log.d(TAG, "onToggle Like Clicked")
				orderViewModel.toggleLikeProduct(productId)
			}

			override fun onDeleteClick(itemId: String) {
				Log.d(TAG, "onDelete: initiated")
				showDeleteDialog(itemId)
			}

			override fun onPlusClick(itemId: String) {
				Log.d(TAG, "onPlus: Increasing quantity")
				orderViewModel.setQuantityOfItem(itemId, 1)
			}

			override fun onMinusClick(itemId: String, currQuantity: Int) {
				Log.d(TAG, "onMinus: decreasing quantity")
				if (currQuantity == 1) {
					showDeleteDialog(itemId)
				} else {
					orderViewModel.setQuantityOfItem(itemId, -1)
				}
			}
		}
	}

	private fun navigateToSelectAddress() {
		findNavController().navigate(R.id.action_cartFragment_to_selectAddressFragment)
	}

	private fun showDeleteDialog(itemId: String) {
		context?.let {
			MaterialAlertDialogBuilder(it)
				.setTitle(getString(R.string.delete_dialog_title_text))
				.setMessage(getString(R.string.delete_cart_item_message_text))
				.setNegativeButton(getString(R.string.pro_cat_dialog_cancel_btn)) { dialog, _ ->
					dialog.cancel()
				}
				.setPositiveButton(getString(R.string.delete_dialog_delete_btn_text)) { dialog, _ ->
					orderViewModel.deleteItemFromCart(itemId)
					dialog.cancel()
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