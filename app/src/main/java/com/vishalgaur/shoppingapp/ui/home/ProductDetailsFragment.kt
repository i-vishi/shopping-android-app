package com.vishalgaur.shoppingapp.ui.home

import android.annotation.SuppressLint
import android.app.Application
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.marginStart
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.PagerSnapHelper
import com.google.android.material.chip.Chip
import com.vishalgaur.shoppingapp.R
import com.vishalgaur.shoppingapp.database.products.ShoeColors
import com.vishalgaur.shoppingapp.database.products.ShoeSizes
import com.vishalgaur.shoppingapp.databinding.FragmentProductDetailsBinding
import com.vishalgaur.shoppingapp.ui.DotsIndicatorDecoration
import com.vishalgaur.shoppingapp.viewModels.ProductViewModel
import java.lang.IllegalArgumentException

class ProductDetailsFragment : Fragment() {

    inner class ProductViewModelFactory(
		private val productId: String,
		private val application: Application
	) : ViewModelProvider.Factory {
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

    override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
        binding = FragmentProductDetailsBinding.inflate(layoutInflater)
        val productId = arguments?.getString("productId")

        if (activity != null && productId != null) {
            val viewModelFactory = ProductViewModelFactory(productId, requireActivity().application)
            viewModel = ViewModelProvider(this, viewModelFactory).get(ProductViewModel::class.java)
        }

        setViews()

        setObservers()
        return binding.root
    }

    private fun setViews() {
        binding.addProAppBar.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        if (context != null) {
        	binding.proDetailsImagesRecyclerview.isNestedScrollingEnabled = false
            val adapter = ProductImagesAdapter(
				requireContext(),
				viewModel.productData.value?.images ?: emptyList()
			)
            binding.proDetailsImagesRecyclerview.adapter = adapter
            val rad = resources.getDimension(R.dimen.radius)
            val dotsHeight = resources.getDimensionPixelSize(R.dimen.dots_height)
            val inactiveColor = ContextCompat.getColor(requireContext(), R.color.gray)
            val activeColor = ContextCompat.getColor(requireContext(), R.color.blue_accent_300)
            val itemDecoration =
                DotsIndicatorDecoration(rad, rad * 4, dotsHeight, inactiveColor, activeColor)
            binding.proDetailsImagesRecyclerview.addItemDecoration(itemDecoration)
            PagerSnapHelper().attachToRecyclerView(binding.proDetailsImagesRecyclerview)
        }
        binding.proDetailsTitleTv.text = viewModel.productData.value?.name ?: ""
        binding.proDetailsLikeBtn.apply {
            setOnClickListener {
                changeImage()
            }
        }
        setShoeSizeChips()
        setShoeColorsChips()
        binding.proDetailsSpecificsText.text = viewModel.productData.value?.description ?: ""
    }

    private fun setObservers() {
        viewModel.isLiked.observe(viewLifecycleOwner) {
            if (it == true) {
                binding.proDetailsLikeBtn.setImageResource(R.drawable.liked_heart_drawable)
            } else {
                binding.proDetailsLikeBtn.setImageResource(R.drawable.heart_icon_drawable)
            }
        }
    }

    private fun changeImage() {
        viewModel.toggleLikeProduct()
    }

    @SuppressLint("ResourceAsColor")
    private fun setShoeSizeChips() {
        binding.proDetailsSizesChipGroup.apply {
            for ((k, v) in ShoeSizes) {
                val chip = Chip(context)
                chip.id = v
                chip.tag = v
                chip.text = "$v"

                chip.chipStrokeColor =
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.gray))
//				chip.chipCornerRadius = 100F
//				chip.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16F)
//				chip.setPadding(60)
                chip.checkedIcon = null
                chip.chipStrokeWidth = TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_DIP,
					1F,
					context.resources.displayMetrics
				)
                if (viewModel.productData.value?.availableSizes?.contains(v) == true) {
                    chip.isCheckable = true
                    chip.isEnabled = true
                    chip.setTextColor(Color.BLACK)
                    chip.chipBackgroundColor = ColorStateList.valueOf(Color.TRANSPARENT)
                    chip.setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            chip.chipStrokeColor = ColorStateList.valueOf(
								ContextCompat.getColor(
									context,
									R.color.blue_accent_300
								)
							)
                        } else {
                            chip.chipStrokeColor = ColorStateList.valueOf(
								ContextCompat.getColor(
									context,
									R.color.gray
								)
							)
                        }
                    }
                } else {
                    chip.isCheckable = false
                    chip.isEnabled = false
                    chip.chipBackgroundColor =
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.gray))
                }

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
                chip.chipCornerRadius = TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_DIP,
					40F,
					context.resources.displayMetrics
				)
                chip.chipStrokeColor =
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.blue_accent_300))
                chip.chipStrokeWidth = TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_DIP,
					1F,
					context.resources.displayMetrics
				)
                chip.chipBackgroundColor = ColorStateList.valueOf(Color.parseColor(v))
                chip.isEnabled = viewModel.productData.value?.availableColors?.contains(k) == true

                chip.isCheckable = viewModel.productData.value?.availableColors?.contains(k) == true

                if (viewModel.productData.value?.availableColors?.contains(k) == false) {
                    chip.chipStrokeWidth = TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP,
						4F,
						context.resources.displayMetrics
					)
                    chip.chipStrokeColor =
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.gray))
                }

                addView(chip)
                ind++
            }
            invalidate()
        }
    }

}