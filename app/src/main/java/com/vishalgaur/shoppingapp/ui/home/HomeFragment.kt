package com.vishalgaur.shoppingapp.ui.home

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vishalgaur.shoppingapp.R
import com.vishalgaur.shoppingapp.data.Product
import com.vishalgaur.shoppingapp.data.utils.ProductCategories
import com.vishalgaur.shoppingapp.data.utils.StoreDataStatus
import com.vishalgaur.shoppingapp.databinding.FragmentHomeBinding
import com.vishalgaur.shoppingapp.ui.MyOnFocusChangeListener
import com.vishalgaur.shoppingapp.ui.RecyclerViewPaddingItemDecoration
import com.vishalgaur.shoppingapp.viewModels.HomeViewModel
import com.vishalgaur.shoppingapp.viewModels.HomeViewModelFactory


private const val TAG = "HomeFragment"

class HomeFragment : Fragment() {

	private lateinit var binding: FragmentHomeBinding
	private lateinit var viewModel: HomeViewModel
	private val focusChangeListener = MyOnFocusChangeListener()

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		// Inflate the layout for this fragment
		binding = FragmentHomeBinding.inflate(layoutInflater)
		if (activity != null) {
			val viewModelFactory = HomeViewModelFactory(requireActivity().application)
			viewModel = ViewModelProvider(this, viewModelFactory).get(HomeViewModel::class.java)
		}

		setViews()

		setObservers()

		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		viewModel.products.observe(viewLifecycleOwner) { productsList ->
			if (context != null) {
				val productAdapter = ProductAdapter(productsList ?: emptyList(), requireContext())
				productAdapter.onClickListener = object : ProductAdapter.OnClickListener {
					override fun onClick(productData: Product) {
						Log.d(TAG, "Product: ${productData.productId} clicked")
						findNavController().navigate(
							R.id.action_seeProduct,
							bundleOf("productId" to productData.productId)
						)
					}

					override fun onDeleteClick(productData: Product) {
						Log.d(
							TAG,
							"onDeleteProduct: deletion initiated for ${productData.productId}"
						)
						showDeleteDialog(productData.name, productData.productId)
					}

					override fun onEditClick(productId: String) {
						Log.d(TAG, "onEditProduct: Edit Initiated for $productId")
						navigateToAddEditProductFragment(isEdit = true, productId = productId)
					}
				}
				binding.productsRecyclerView.apply {
					adapter = productAdapter
					val itemDecoration = RecyclerViewPaddingItemDecoration(requireContext())
					if (itemDecorationCount == 0) {
						addItemDecoration(itemDecoration)
					}
				}
			}
		}
	}

	private fun setViews() {
		binding.homeTopAppBar.topAppBar.inflateMenu(R.menu.home_app_bar_menu)
		binding.homeTopAppBar.homeSearchEditText.onFocusChangeListener = focusChangeListener
		binding.homeTopAppBar.homeSearchEditText.setOnEditorActionListener { textView, actionId, event ->
			if (actionId == EditorInfo.IME_ACTION_SEARCH) {
				textView.clearFocus()
				val inputManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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
			val inputManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
			inputManager.hideSoftInputFromWindow(it.windowToken, 0)
			viewModel.filterProducts("All")
		}
		binding.homeTopAppBar.topAppBar.setOnMenuItemClickListener { menuItem ->
			setAppBarItemClicks(menuItem)
		}
		if (!viewModel.isUserASeller) {
			binding.homeFabAddProduct.visibility = View.GONE
		}
		binding.homeFabAddProduct.setOnClickListener {
			showDialogWithItems(ProductCategories, 0, false)
		}
		binding.loaderLayout.circularLoader.visibility = View.GONE
	}

	private fun performSearch(query: String) {
		Log.d(TAG, "query = $query")
		viewModel.filterBySearch(query)
	}

	private fun setAppBarItemClicks(menuItem: MenuItem): Boolean {
		return when (menuItem.itemId) {
			R.id.home_filter -> {
				val extraFilters = arrayOf("All", "None")
				showDialogWithItems(ProductCategories.plus(extraFilters), -1, true)
				true
			}
			else -> false
		}
	}

	private fun setObservers() {
		viewModel.storeDataStatus.observe(viewLifecycleOwner) { status ->
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
		viewModel.allProducts.observe(viewLifecycleOwner) {
			if (it != null) {
				viewModel.filterProducts("All")
			}
		}
	}

	private fun showDeleteDialog(productName: String, productId: String) {
		context?.let {
			MaterialAlertDialogBuilder(it)
				.setTitle(getString(R.string.delete_dialog_title_text))
				.setMessage(getString(R.string.delete_dialog_message_text, productName))
				.setNeutralButton(getString(R.string.pro_cat_dialog_cancel_btn)) { dialog, _ ->
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
				.setNeutralButton(getString(R.string.pro_cat_dialog_cancel_btn)) { dialog, _ ->
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
}