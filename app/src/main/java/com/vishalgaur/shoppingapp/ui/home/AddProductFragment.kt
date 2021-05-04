package com.vishalgaur.shoppingapp.ui.home

import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.vishalgaur.shoppingapp.R
import com.vishalgaur.shoppingapp.data.utils.AddProductErrors
import com.vishalgaur.shoppingapp.data.utils.ShoeColors
import com.vishalgaur.shoppingapp.data.utils.ShoeSizes
import com.vishalgaur.shoppingapp.databinding.FragmentAddProductBinding
import com.vishalgaur.shoppingapp.ui.AddProductViewErrors
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
    private var imgList = mutableListOf<Uri>()

    private val getImages =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { result ->
            imgList.addAll(result)
            if (imgList.size > 3) {
                imgList = imgList.subList(0, 3)
                makeToast("Maximum 3 images are allowed!")
            }
            val adapter = AddProductImagesAdapter(imgList)
            binding.addProImagesRv.adapter = adapter
        }

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
        viewModel.errorStatus.observe(viewLifecycleOwner) { err ->
            modifyErrors(err)
        }

        viewModel.addProductErrors.observe(viewLifecycleOwner) { status ->
            when (status) {
                AddProductErrors.ADDING -> {
                    binding.loaderLayout.circularLoader.visibility = View.VISIBLE
                    binding.loaderLayout.circularLoader.showAnimationBehavior
                }
                AddProductErrors.ERR_ADD -> {
                    binding.loaderLayout.circularLoader.visibility = View.GONE
                    binding.loaderLayout.circularLoader.hideAnimationBehavior
                    binding.addProErrorTextView.visibility = View.VISIBLE
                    binding.addProErrorTextView.text =
                        getString(R.string.add_product_error_img_upload)
                }
                AddProductErrors.NONE -> {
                    binding.loaderLayout.circularLoader.visibility = View.GONE
                    binding.loaderLayout.circularLoader.hideAnimationBehavior
                }
            }
        }
    }

    private fun setViews() {
        binding.addProAppBar.topAppBar.title = "Add Product - ${viewModel.selectedCategory.value}"
        binding.addProAppBar.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.loaderLayout.circularLoader.visibility = View.GONE

        val adapter = AddProductImagesAdapter(imgList)
        binding.addProImagesRv.adapter = adapter

        binding.addProErrorTextView.visibility = View.GONE
        binding.proNameEditText.onFocusChangeListener = focusChangeListener
        binding.proPriceEditText.onFocusChangeListener = focusChangeListener
        binding.proDescEditText.onFocusChangeListener = focusChangeListener

        binding.addProImagesBtn.setOnClickListener {
            getImages.launch("image/*")
        }

        setShoeSizesChips()
        setShoeColorsChips()

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
        Log.d(TAG, "onAddProduct: Add product initiated")
        viewModel.submitProduct(
            name, price, mrp, desc, sizeList.toList(), colorsList.toList(), imgList
        )
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
                chip.chipStrokeWidth = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    1F,
                    context.resources.displayMetrics
                )
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