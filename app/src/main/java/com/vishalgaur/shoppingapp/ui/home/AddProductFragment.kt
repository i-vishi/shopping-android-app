package com.vishalgaur.shoppingapp.ui.home

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.vishalgaur.shoppingapp.R
import com.vishalgaur.shoppingapp.database.products.ShoeColors
import com.vishalgaur.shoppingapp.database.products.ShoeSizes
import com.vishalgaur.shoppingapp.databinding.FragmentAddProductBinding
import com.vishalgaur.shoppingapp.ui.AddProductErrors
import com.vishalgaur.shoppingapp.ui.MyOnFocusChangeListener
import com.vishalgaur.shoppingapp.viewModels.HomeViewModel
import com.vishalgaur.shoppingapp.viewModels.HomeViewModelFactory

private const val TAG = "AddProductFragment"

class AddProductFragment : Fragment() {

	private lateinit var binding: FragmentAddProductBinding
	private lateinit var viewModel: HomeViewModel
	private val focusChangeListener = MyOnFocusChangeListener()

	private var sizeList = mutableSetOf<Int>()
	private var colorsList = mutableSetOf<String>()

	override fun onCreateView(
			inflater: LayoutInflater, container: ViewGroup?,
			savedInstanceState: Bundle?
	): View? {
		// Inflate the layout for this fragment
		binding = FragmentAddProductBinding.inflate(layoutInflater)
		if (activity != null) {
			val viewModelFactory = HomeViewModelFactory(requireActivity().application)
			viewModel = ViewModelProvider(this, viewModelFactory).get(HomeViewModel::class.java)
		}
		val catName = arguments?.getString("categoryName")
		if (catName != null) {
			viewModel.setCategory(catName)
		}
		setViews()

		setObservers()
		return binding.root
	}

	private fun setObservers() {
		viewModel.errorStatus.observe(viewLifecycleOwner) {
			when (it) {
				AddProductErrors.NONE -> binding.addProErrorTextView.visibility = View.GONE
				else -> binding.addProErrorTextView.visibility = View.VISIBLE
			}
		}
	}

	private fun setViews() {
		binding.addProAppBar.topAppBar.title = "Add Product - ${viewModel.selectedCategory.value}"

		binding.addProErrorTextView.visibility = View.GONE
		binding.proNameEditText.onFocusChangeListener = focusChangeListener
		binding.proPriceEditText.onFocusChangeListener = focusChangeListener
		binding.proDescEditText.onFocusChangeListener = focusChangeListener

		setShoeSizesChips()
		setShoeColorsChips()

		binding.addProBtn.setOnClickListener { onAddProduct() }
	}

	private fun onAddProduct() {
		val name = binding.proNameEditText.text.toString()
		val price = binding.proPriceEditText.text.toString().toDoubleOrNull()
		val desc = binding.proDescEditText.text.toString()
		viewModel.submitProduct(name, price, desc, sizeList.toList())
	}

	private fun setShoeSizesChips() {
		binding.addProSizeChipGroup.apply {
			for ((_, v) in ShoeSizes) {
				val chip = Chip(context)
				chip.id = v
				chip.tag = v

				chip.text = "$v"
				chip.isCheckable = true

				chip.setOnCheckedChangeListener { buttonView, isChecked ->
					val tag = buttonView.tag.toString().toInt()
					if (!isChecked) {
						sizeList.remove(tag)
					} else {
						sizeList.add(tag)
					}
				}
				addView(chip)
			}
			invalidate()
		}
	}

	private fun setShoeColorsChips() {
		binding.addProColorChipGroup.apply {
			var ind = 1
			for ((k, v) in ShoeColors) {
				val chip = Chip(context)
				chip.id = ind
				chip.tag = k

				chip.chipStrokeColor = ColorStateList.valueOf(Color.BLACK)
				chip.chipStrokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1F, context.resources.displayMetrics)
				chip.chipBackgroundColor = ColorStateList.valueOf(Color.parseColor(v))
				chip.isCheckable = true

				chip.setOnCheckedChangeListener { buttonView, isChecked ->
					val tag = buttonView.tag.toString()
					if (!isChecked) {
						colorsList.remove(tag)
					} else {
						colorsList.add(tag)
					}
				}
				addView(chip)
				ind++
			}
			invalidate()
		}
	}
}