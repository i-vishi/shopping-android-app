package com.vishalgaur.shoppingapp.ui.home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vishalgaur.shoppingapp.R
import com.vishalgaur.shoppingapp.data.Product
import com.vishalgaur.shoppingapp.data.utils.ProductCategories
import com.vishalgaur.shoppingapp.data.utils.StoreDataStatus
import com.vishalgaur.shoppingapp.databinding.FragmentHomeBinding
import com.vishalgaur.shoppingapp.ui.RecyclerViewPaddingItemDecoration
import com.vishalgaur.shoppingapp.viewModels.HomeViewModel
import com.vishalgaur.shoppingapp.viewModels.HomeViewModelFactory

private const val TAG = "HomeFragment"

class HomeFragment : Fragment() {

	private lateinit var binding: FragmentHomeBinding
	private lateinit var viewModel: HomeViewModel

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
			if(context != null) {
				val adapter = ProductAdapter(productsList, requireContext())
				adapter.onClickListener = object : ProductAdapter.OnClickListener {
					override fun onClick(productData: Product) {
						Log.d(TAG, "Product: ${productData.productId} clicked")
						findNavController().navigate(R.id.action_seeProduct, bundleOf("productId" to productData.productId))
					}

					override fun onDeleteClick(productData: Product) {
						Log.d(TAG, "onDeleteProduct: deletion initiated for ${productData.productId}")
						showDeleteDialog(productData.name, productData.productId)
					}

					override fun onEditClick(productId: String) {
						Log.d(TAG, "onEditProduct: Edit Initiated for $productId")
					}
				}
				binding.productsRecyclerView.adapter = adapter
				val itemDecoration = RecyclerViewPaddingItemDecoration(requireContext())
				binding.productsRecyclerView.addItemDecoration(itemDecoration)
			}
		}
	}

	private fun setViews() {
		if(!viewModel.isUserASeller) {
			binding.homeFabAddProduct.visibility = View.GONE
		}
		binding.homeFabAddProduct.setOnClickListener {
			showDialog()
		}
		binding.loaderLayout.circularLoader.visibility = View.GONE
	}

	private fun setObservers() {
		viewModel.storeDataStatus.observe(viewLifecycleOwner) { status ->
			when(status) {
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

	private fun showDialog() {
		val categoryItems = ProductCategories
		var checkedItem = 0
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
						navigateToAddProductFragment(categoryItems[checkedItem])
						dialog.cancel()
					}
					.show()
		}
	}

	private fun navigateToAddProductFragment(catName: String) {
		viewModel.setCategory(catName)
		findNavController().navigate(R.id.action_goto_addProduct, bundleOf("categoryName" to catName))
	}
}