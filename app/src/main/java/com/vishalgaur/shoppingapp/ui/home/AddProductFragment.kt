package com.vishalgaur.shoppingapp.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.vishalgaur.shoppingapp.databinding.FragmentAddProductBinding
import com.vishalgaur.shoppingapp.ui.MyOnFocusChangeListener
import com.vishalgaur.shoppingapp.viewModels.HomeViewModel
import com.vishalgaur.shoppingapp.viewModels.HomeViewModelFactory

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class AddProductFragment : Fragment() {

	private lateinit var binding: FragmentAddProductBinding
	private lateinit var viewModel: HomeViewModel
	private val focusChangeListener = MyOnFocusChangeListener()

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

		setViews()

//		setObservers()
		return binding.root
	}

	private fun setObservers() {
		TODO("Not yet implemented")
	}

	private fun setViews() {
		binding.proNameEditText.onFocusChangeListener = focusChangeListener
		binding.proPriceEditText.onFocusChangeListener = focusChangeListener
		binding.proDescEditText.onFocusChangeListener = focusChangeListener

		binding.addProBtn.setOnClickListener(object : View.OnClickListener{
			override fun onClick(v: View?) {
				onAddProduct()
			}
		})
	}

	private fun onAddProduct() {
		TODO("Not yet implemented")
	}
}