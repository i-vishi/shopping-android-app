package com.vishalgaur.shoppingapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.vishalgaur.shoppingapp.R
import com.vishalgaur.shoppingapp.data.utils.StoreDataStatus
import com.vishalgaur.shoppingapp.databinding.FragmentOrderSuccessBinding
import com.vishalgaur.shoppingapp.viewModels.OrderViewModel

class OrderSuccessFragment : Fragment() {

	private lateinit var binding: FragmentOrderSuccessBinding
	private val orderViewModel: OrderViewModel by activityViewModels()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		binding = FragmentOrderSuccessBinding.inflate(layoutInflater)

		binding.loaderLayout.loaderFrameLayout.background = ResourcesCompat.getDrawable(resources, R.color.white, null)
		binding.orderConstraintGroup.visibility = View.GONE
		setObservers()
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		binding.backToHomeBtn.setOnClickListener {
			findNavController().navigate(R.id.action_orderSuccessFragment_to_homeFragment)
		}
	}

	private fun setObservers() {
		orderViewModel.orderStatus.observe(viewLifecycleOwner) { status ->
			when (status) {
				StoreDataStatus.LOADING -> {
					binding.loaderLayout.circularLoader.showAnimationBehavior
					binding.loaderLayout.loaderFrameLayout.visibility = View.VISIBLE
				}
				else -> {
					binding.orderConstraintGroup.visibility = View.VISIBLE
					binding.loaderLayout.circularLoader.showAnimationBehavior
					binding.loaderLayout.loaderFrameLayout.visibility = View.GONE
				}
			}
		}
	}
}