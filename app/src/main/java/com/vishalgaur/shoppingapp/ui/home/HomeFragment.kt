package com.vishalgaur.shoppingapp.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vishalgaur.shoppingapp.R
import com.vishalgaur.shoppingapp.data.Product
import com.vishalgaur.shoppingapp.data.utils.ProductCategories
import com.vishalgaur.shoppingapp.data.utils.StoreDataStatus
import com.vishalgaur.shoppingapp.databinding.FragmentHomeBinding
import com.vishalgaur.shoppingapp.ui.MyOnFocusChangeListener
import com.vishalgaur.shoppingapp.ui.RecyclerViewPaddingItemDecoration
import com.vishalgaur.shoppingapp.viewModels.HomeViewModel
import kotlinx.coroutines.*


private const val TAG = "HomeFragment"

class HomeFragment : Fragment() {

	private lateinit var binding: FragmentHomeBinding
	private val viewModel: HomeViewModel by activityViewModels()
	private val focusChangeListener = MyOnFocusChangeListener()
	private lateinit var productAdapter: ProductAdapter

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		// Inflate the layout for this fragment
		binding = FragmentHomeBinding.inflate(layoutInflater)
		setViews()
		setObservers()
		return binding.root
	}

	private fun setViews() {
		setHomeTopAppBar()
		if (context != null) {
			setProductsAdapter(viewModel.products.value)
			binding.productsRecyclerView.apply {
				val gridLayoutManager = GridLayoutManager(context, 2)
				gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
					override fun getSpanSize(position: Int): Int {
						return when (productAdapter.getItemViewType(position)) {
							2 -> 2 //ad
							else -> {
								// product, full for last item
								if (position + 1 == productAdapter.data.size) 2 else 1
							}
						}
					}
				}
				layoutManager = gridLayoutManager
				adapter = productAdapter
				val itemDecoration = RecyclerViewPaddingItemDecoration(requireContext())
				if (itemDecorationCount == 0) {
					addItemDecoration(itemDecoration)
				}
			}
		}

		if (!viewModel.isUserASeller) {
			binding.homeFabAddProduct.visibility = View.GONE
		}
		binding.homeFabAddProduct.setOnClickListener {
			showDialogWithItems(ProductCategories, 0, false)
		}
		binding.loaderLayout.loaderFrameLayout.visibility = View.GONE
	}

	private fun setObservers() {
		viewModel.storeDataStatus.observe(viewLifecycleOwner) { status ->
			when (status) {
				StoreDataStatus.LOADING -> {
					binding.loaderLayout.loaderFrameLayout.visibility = View.VISIBLE
					binding.loaderLayout.circularLoader.showAnimationBehavior
					binding.productsRecyclerView.visibility = View.GONE
				}
				else -> {
					binding.loaderLayout.circularLoader.hideAnimationBehavior
					binding.loaderLayout.loaderFrameLayout.visibility = View.GONE
				}
			}
			if (status != null && status != StoreDataStatus.LOADING) {
				viewModel.products.observe(viewLifecycleOwner) { productsList ->
					if (productsList.isNotEmpty()) {
						binding.productsRecyclerView.visibility = View.VISIBLE
						binding.productsRecyclerView.adapter?.apply {
							productAdapter.data =
								getMixedDataList(productsList, getAdsList())
							notifyDataSetChanged()
						}
					}
				}
			}
		}
		viewModel.allProducts.observe(viewLifecycleOwner) {
			if (it.isNotEmpty()) {
				viewModel.setDataLoaded()
				viewModel.filterProducts("All")
			}
		}
	}

	private fun performSearch(query: String) {
		viewModel.filterBySearch(query)
	}

	private fun setAppBarItemClicks(menuItem: MenuItem): Boolean {
		return when (menuItem.itemId) {
			R.id.home_filter -> {
				val extraFilters = arrayOf("All", "None")
				val categoryList = ProductCategories.plus(extraFilters)
				val checkedItem = categoryList.indexOf(viewModel.filterCategory.value)
				showDialogWithItems(categoryList, checkedItem, true)
				true
			}
			R.id.home_favorites -> {
				// show favorite products list
				findNavController().navigate(R.id.action_homeFragment_to_favoritesFragment)
				true
			}
			else -> false
		}
	}

	private fun setHomeTopAppBar() {
		var lastInput = ""
		val debounceJob: Job? = null
		val uiScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
		binding.homeTopAppBar.topAppBar.inflateMenu(R.menu.home_app_bar_menu)
		if (viewModel.isUserASeller) {
			binding.homeTopAppBar.topAppBar.menu.removeItem(R.id.home_favorites)
		}
		binding.homeTopAppBar.homeSearchEditText.onFocusChangeListener = focusChangeListener
		binding.homeTopAppBar.homeSearchEditText.doAfterTextChanged { editable ->
			if (editable != null) {
				val newtInput = editable.toString()
				debounceJob?.cancel()
				if (lastInput != newtInput) {
					lastInput = newtInput
					uiScope.launch {
						delay(500)
						if (lastInput == newtInput) {
							performSearch(newtInput)
						}
					}
				}
			}
		}
		binding.homeTopAppBar.homeSearchEditText.setOnEditorActionListener { textView, actionId, _ ->
			if (actionId == EditorInfo.IME_ACTION_SEARCH) {
				textView.clearFocus()
				val inputManager =
					requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
				inputManager.hideSoftInputFromWindow(textView.windowToken, 0)
				performSearch(textView.text.toString())
				true
			} else {
				false
			}
		}
		binding.homeTopAppBar.searchOutlinedTextLayout.setEndIconOnClickListener {
			it.clearFocus()
			binding.homeTopAppBar.homeSearchEditText.setText("")
			val inputManager =
				requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
			inputManager.hideSoftInputFromWindow(it.windowToken, 0)
//			viewModel.filterProducts("All")
		}
		binding.homeTopAppBar.topAppBar.setOnMenuItemClickListener { menuItem ->
			setAppBarItemClicks(menuItem)
		}
	}

	private fun setProductsAdapter(productsList: List<Product>?) {
		productAdapter = ProductAdapter(productsList ?: emptyList(), requireContext())
		productAdapter.onClickListener = object : ProductAdapter.OnClickListener {
			override fun onClick(productData: Product) {
				findNavController().navigate(
					R.id.action_seeProduct,
					bundleOf("productId" to productData.productId)
				)
			}

			override fun onDeleteClick(productData: Product) {
				Log.d(TAG, "onDeleteProduct: initiated for ${productData.productId}")
				showDeleteDialog(productData.name, productData.productId)
			}

			override fun onEditClick(productId: String) {
				Log.d(TAG, "onEditProduct: initiated for $productId")
				navigateToAddEditProductFragment(isEdit = true, productId = productId)
			}

			override fun onLikeClick(productId: String) {
				Log.d(TAG, "onToggleLike: initiated for $productId")
				viewModel.toggleLikeByProductId(productId)
			}

			override fun onAddToCartClick(productData: Product) {
				Log.d(TAG, "onToggleCartAddition: initiated")
				viewModel.toggleProductInCart(productData)
			}
		}
		productAdapter.bindImageButtons = object : ProductAdapter.BindImageButtons {
			@SuppressLint("ResourceAsColor")
			override fun setLikeButton(productId: String, button: CheckBox) {
				button.isChecked = viewModel.isProductLiked(productId)
			}

			override fun setCartButton(productId: String, imgView: ImageView) {
				if (viewModel.isProductInCart(productId)) {
					imgView.setImageResource(R.drawable.ic_remove_shopping_cart_24)
				} else {
					imgView.setImageResource(R.drawable.ic_add_shopping_cart_24)
				}
			}

		}
	}

	private fun showDeleteDialog(productName: String, productId: String) {
		context?.let {
			MaterialAlertDialogBuilder(it)
				.setTitle(getString(R.string.delete_dialog_title_text))
				.setMessage(getString(R.string.delete_dialog_message_text, productName))
				.setNegativeButton(getString(R.string.pro_cat_dialog_cancel_btn)) { dialog, _ ->
					dialog.cancel()
				}
				.setPositiveButton(getString(R.string.delete_dialog_delete_btn_text)) { dialog, _ ->
					viewModel.deleteProduct(productId)
					dialog.cancel()
				}
				.show()
		}
	}

	private fun showDialogWithItems(
		categoryItems: Array<String>,
		checkedOption: Int = 0,
		isFilter: Boolean
	) {
		var checkedItem = checkedOption
		context?.let {
			MaterialAlertDialogBuilder(it)
				.setTitle(getString(R.string.pro_cat_dialog_title))
				.setSingleChoiceItems(categoryItems, checkedItem) { _, which ->
					checkedItem = which
				}
				.setNegativeButton(getString(R.string.pro_cat_dialog_cancel_btn)) { dialog, _ ->
					dialog.cancel()
				}
				.setPositiveButton(getString(R.string.pro_cat_dialog_ok_btn)) { dialog, _ ->
					if (checkedItem == -1) {
						dialog.cancel()
					} else {
						if (isFilter) {
							viewModel.filterProducts(categoryItems[checkedItem])
						} else {
							navigateToAddEditProductFragment(
								isEdit = false,
								catName = categoryItems[checkedItem]
							)
						}
					}
					dialog.cancel()
				}
				.show()
		}
	}

	private fun navigateToAddEditProductFragment(
		isEdit: Boolean,
		catName: String? = null,
		productId: String? = null
	) {
		findNavController().navigate(
			R.id.action_goto_addProduct,
			bundleOf("isEdit" to isEdit, "categoryName" to catName, "productId" to productId)
		)
	}

	private fun getMixedDataList(data: List<Product>, adsList: List<Int>): List<Any> {
		val itemsList = mutableListOf<Any>()
		itemsList.addAll(data)
		var currPos = 0
		if (itemsList.size >= 4) {
			adsList.forEach label@{ ad ->
				if (itemsList.size > currPos + 1) {
					itemsList.add(currPos, ad)
				} else {
					return@label
				}
				currPos += 5
			}
		}
		return itemsList
	}

	private fun getAdsList(): List<Int> {
		return listOf(R.drawable.ad_ex_2, R.drawable.ad_ex_1, R.drawable.ad_ex_3)
	}
}