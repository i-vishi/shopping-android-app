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
import com.vishalgaur.shoppingapp.databinding.FragmentHomeBinding
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

		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		val adapter = ProductAdapter(viewModel.products.value ?: ArrayList())
		adapter.setOnItemClickListener {
			Log.d(TAG, "product clicked: $it")
		}
		binding.productsRecyclerView.adapter = adapter
	}

	private fun setViews() {
		binding.homeFabAddProduct.setOnClickListener {
			showDialog()
		}
	}


	private fun showDialog() {
		val categoryItems = arrayOf("Shoes")
		var checkedItem = -1
		context?.let {
			MaterialAlertDialogBuilder(it)
					.setTitle(getString(R.string.pro_cat_dialog_title))
					.setSingleChoiceItems(categoryItems, checkedItem) { dialog, which ->
						checkedItem = which
					}
					.setNegativeButton(getString(R.string.pro_cat_dialog_cancel_btn)) { dialog, _ ->
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