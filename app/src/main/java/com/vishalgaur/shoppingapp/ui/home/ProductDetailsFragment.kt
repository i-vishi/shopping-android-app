package com.vishalgaur.shoppingapp.ui.home

import android.annotation.SuppressLint
import android.app.Application
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.chip.Chip
import com.vishalgaur.shoppingapp.R
import com.vishalgaur.shoppingapp.database.products.ShoeColors
import com.vishalgaur.shoppingapp.database.products.ShoeSizes
import com.vishalgaur.shoppingapp.databinding.FragmentProductDetailsBinding
import com.vishalgaur.shoppingapp.viewModels.ProductViewModel
import java.lang.IllegalArgumentException

class ProductDetailsFragment : Fragment() {

	inner class ProductViewModelFactory(private val productId: String, private val application: Application) : ViewModelProvider.Factory {
		@Suppress("UNCHECKED_CAST")
		override fun <T : ViewModel?> create(modelClass: Class<T>): T {
			if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
				return ProductViewModel(productId, application) as T
			}
			throw IllegalArgumentException("Unknown ViewModel Class")
		}
	}

	private lateinit var binding: FragmentProductDetailsBinding
	private lateinit var viewModel: ProductViewModel

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		binding = FragmentProductDetailsBinding.inflate(layoutInflater)
		val productId = arguments?.getString("productId")

		if (activity != null && productId != null) {
			val viewModelFactory = ProductViewModelFactory(productId, requireActivity().application)
			viewModel = ViewModelProvider(this, viewModelFactory).get(ProductViewModel::class.java)
		}

		setViews()

//		setObservers()
		return binding.root
	}

	private fun setViews() {
		binding.proDetailsTitleTv.text = viewModel.productData.value?.name ?: ""
		setShoeSizeChips()
		setShoeColorsChips()
		binding.proDetailsSpecificsText.text = viewModel.productData.value?.description ?: ""
	}

	@SuppressLint("ResourceAsColor")
	private fun setShoeSizeChips() {
		binding.proDetailsSizesChipGroup.apply {
			for ((k, v) in ShoeSizes) {
				val chip = Chip(context)
				chip.id = v
				chip.tag = v
				chip.text = "$v"

				chip.chipStrokeColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.blue_accent_300))

				chip.isCheckable = viewModel.productData.value?.availableSizes?.contains(v) == true

				addView(chip)
			}
			invalidate()
		}
	}

	private fun setShoeColorsChips() {
		binding.proDetailsColorsChipGroup.apply {
			var ind = 1
			for ((k, v) in ShoeColors) {
				val chip = Chip(context)
				chip.id = ind
				chip.tag = k
				chip.text = ".."
				chip.setTextColor(ColorStateList.valueOf(Color.parseColor(v)))
				chip.chipCornerRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40F, context.resources.displayMetrics)
				chip.chipStrokeColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.blue_accent_300))
				chip.chipStrokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1F, context.resources.displayMetrics)
				chip.chipBackgroundColor = ColorStateList.valueOf(Color.parseColor(v))
				chip.isCheckable = viewModel.productData.value?.availableSizes?.contains(v) == true

				addView(chip)
				ind++
			}
			invalidate()
		}
	}

//	private fun setObservers() {
//		TODO("Not yet implemented")
//	}
}