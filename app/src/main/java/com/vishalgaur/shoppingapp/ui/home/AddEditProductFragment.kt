package com.vishalgaur.shoppingapp.ui.home

import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.vishalgaur.shoppingapp.R
import com.vishalgaur.shoppingapp.data.utils.AddProductErrors
import com.vishalgaur.shoppingapp.data.utils.ShoeColors
import com.vishalgaur.shoppingapp.data.utils.ShoeSizes
import com.vishalgaur.shoppingapp.data.utils.StoreDataStatus
import com.vishalgaur.shoppingapp.databinding.FragmentAddEditProductBinding
import com.vishalgaur.shoppingapp.ui.AddProductViewErrors
import com.vishalgaur.shoppingapp.ui.MyOnFocusChangeListener
import com.vishalgaur.shoppingapp.viewModels.AddEditProductViewModel
import kotlin.properties.Delegates

private const val TAG = "AddProductFragment"

class AddEditProductFragment : Fragment() {

	private lateinit var binding: FragmentAddEditProductBinding
	private val viewModel by viewModels<AddEditProductViewModel>()
	private val focusChangeListener = MyOnFocusChangeListener()

	// arguments
	private var isEdit by Delegates.notNull<Boolean>()
	private lateinit var catName: String
	private lateinit var productId: String

	private var sizeList = mutableSetOf<Int>()
	private var colorsList = mutableSetOf<String>()
	private var imgList = mutableListOf<Uri>()

	private val getImages =
		registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { result ->
			imgList.addAll(result)
			if (imgList.size > 3) {
				imgList = imgList.subList(0, 3)
				makeToast("Maximum 3 images are allowed!")
			}
			val adapter = context?.let { AddProductImagesAdapter(it, imgList) }
			binding.addProImagesRv.adapter = adapter
		}

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		// Inflate the layout for this fragment
		binding = FragmentAddEditProductBinding.inflate(layoutInflater)

		isEdit = arguments?.getBoolean("isEdit") == true
		catName = arguments?.getString("categoryName").toString()
		productId = arguments?.getString("productId").toString()

		initViewModel()

		setViews()

		setObservers()
		return binding.root
	}

	private fun initViewModel() {
		Log.d(TAG, "init view model, isedit = $isEdit")

		viewModel.setIsEdit(isEdit)
		if (isEdit) {
			Log.d(TAG, "init view model, isedit = true, $productId")
			viewModel.setProductData(productId)
		} else {
			Log.d(TAG, "init view model, isedit = false, $catName")
			viewModel.setCategory(catName)
		}
	}

	private fun setObservers() {
		viewModel.errorStatus.observe(viewLifecycleOwner) { err ->
			modifyErrors(err)
		}
		viewModel.dataStatus.observe(viewLifecycleOwner) { status ->
			when (status) {
				StoreDataStatus.LOADING -> {
					binding.loaderLayout.loaderFrameLayout.visibility = View.VISIBLE
					binding.loaderLayout.circularLoader.showAnimationBehavior
				}
				StoreDataStatus.DONE -> {
					binding.loaderLayout.loaderFrameLayout.visibility = View.GONE
					binding.loaderLayout.circularLoader.hideAnimationBehavior
					fillDataInAllViews()
				}
				else -> {
					binding.loaderLayout.loaderFrameLayout.visibility = View.GONE
					binding.loaderLayout.circularLoader.hideAnimationBehavior
					makeToast("Error getting Data, Try Again!")
				}
			}
		}
		viewModel.addProductErrors.observe(viewLifecycleOwner) { status ->
			when (status) {
				AddProductErrors.ADDING -> {
					binding.loaderLayout.loaderFrameLayout.visibility = View.VISIBLE
					binding.loaderLayout.circularLoader.showAnimationBehavior
				}
				AddProductErrors.ERR_ADD_IMG -> {
					setAddProductErrors(getString(R.string.add_product_error_img_upload))
				}
				AddProductErrors.ERR_ADD -> {
					setAddProductErrors(getString(R.string.add_product_insert_error))
				}
				AddProductErrors.NONE -> {
					binding.loaderLayout.loaderFrameLayout.visibility = View.GONE
					binding.loaderLayout.circularLoader.hideAnimationBehavior
				}
			}
		}
	}

	private fun setAddProductErrors(errText: String) {
		binding.loaderLayout.loaderFrameLayout.visibility = View.GONE
		binding.loaderLayout.circularLoader.hideAnimationBehavior
		binding.addProErrorTextView.visibility = View.VISIBLE
		binding.addProErrorTextView.text = errText

	}

	private fun fillDataInAllViews() {
		viewModel.productData.value?.let { product ->
			Log.d(TAG, "fill data in views")
			binding.addProAppBar.topAppBar.title = "Edit Product - ${product.name}"
			binding.proNameEditText.setText(product.name)
			binding.proPriceEditText.setText(product.price.toString())
			binding.proMrpEditText.setText(product.mrp.toString())
			binding.proDescEditText.setText(product.description)

			imgList = product.images.map { it.toUri() } as MutableList<Uri>
			val adapter = AddProductImagesAdapter(requireContext(), imgList)
			binding.addProImagesRv.adapter = adapter

			setShoeSizesChips(product.availableSizes)
			setShoeColorsChips(product.availableColors)

			binding.addProBtn.setText(R.string.edit_product_btn_text)
		}

	}

	private fun setViews() {
		Log.d(TAG, "set views")

		if (!isEdit) {
			binding.addProAppBar.topAppBar.title =
				"Add Product - ${viewModel.selectedCategory.value}"

			val adapter = AddProductImagesAdapter(requireContext(), imgList)
			binding.addProImagesRv.adapter = adapter
		}
		binding.addProImagesBtn.setOnClickListener {
			getImages.launch("image/*")
		}

		binding.addProAppBar.topAppBar.setNavigationOnClickListener {
			findNavController().navigateUp()
		}

		binding.loaderLayout.loaderFrameLayout.visibility = View.GONE

		setShoeSizesChips()
		setShoeColorsChips()

		binding.addProErrorTextView.visibility = View.GONE
		binding.proNameEditText.onFocusChangeListener = focusChangeListener
		binding.proPriceEditText.onFocusChangeListener = focusChangeListener
		binding.proMrpEditText.onFocusChangeListener = focusChangeListener
		binding.proDescEditText.onFocusChangeListener = focusChangeListener

		binding.addProBtn.setOnClickListener {
			onAddProduct()
			if (viewModel.errorStatus.value == AddProductViewErrors.NONE) {
				viewModel.addProductErrors.observe(viewLifecycleOwner) { err ->
					if (err == AddProductErrors.NONE) {
						findNavController().navigate(R.id.action_addProductFragment_to_homeFragment)
					}
				}
			}
		}
	}

	private fun onAddProduct() {
		val name = binding.proNameEditText.text.toString()
		val price = binding.proPriceEditText.text.toString().toDoubleOrNull()
		val mrp = binding.proMrpEditText.text.toString().toDoubleOrNull()
		val desc = binding.proDescEditText.text.toString()
		Log.d(
			TAG,
			"onAddProduct: Add product initiated, $name, $price, $mrp, $desc, $sizeList, $colorsList, $imgList"
		)
		viewModel.submitProduct(
			name, price, mrp, desc, sizeList.toList(), colorsList.toList(), imgList
		)
	}

	private fun setShoeSizesChips(shoeList: List<Int>? = emptyList()) {
		binding.addProSizeChipGroup.apply {
			removeAllViews()
			for ((_, v) in ShoeSizes) {
				val chip = Chip(context)
				chip.id = v
				chip.tag = v

				chip.text = "$v"
				chip.isCheckable = true

				if (shoeList?.contains(v) == true) {
					chip.isChecked = true
					sizeList.add(chip.tag.toString().toInt())
				}

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

	private fun setShoeColorsChips(colorList: List<String>? = emptyList()) {
		binding.addProColorChipGroup.apply {
			removeAllViews()
			var ind = 1
			for ((k, v) in ShoeColors) {
				val chip = Chip(context)
				chip.id = ind
				chip.tag = k

				chip.chipStrokeColor = ColorStateList.valueOf(Color.BLACK)
				chip.chipStrokeWidth = TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_DIP,
					1F,
					context.resources.displayMetrics
				)
				chip.chipBackgroundColor = ColorStateList.valueOf(Color.parseColor(v))
				chip.isCheckable = true

				if (colorList?.contains(k) == true) {
					chip.isChecked = true
					colorsList.add(chip.tag.toString())
				}

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

	private fun modifyErrors(err: AddProductViewErrors) {
		when (err) {
			AddProductViewErrors.NONE -> binding.addProErrorTextView.visibility = View.GONE
			AddProductViewErrors.EMPTY -> {
				binding.addProErrorTextView.visibility = View.VISIBLE
				binding.addProErrorTextView.text = getString(R.string.add_product_error_string)
			}
			AddProductViewErrors.ERR_PRICE_0 -> {
				binding.addProErrorTextView.visibility = View.VISIBLE
				binding.addProErrorTextView.text = getString(R.string.add_pro_error_price_string)
			}
		}
	}

	private fun makeToast(text: String) {
		Toast.makeText(context, text, Toast.LENGTH_LONG).show()
	}
}