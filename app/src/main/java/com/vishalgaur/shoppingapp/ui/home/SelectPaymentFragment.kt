package com.vishalgaur.shoppingapp.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.vishalgaur.shoppingapp.R
import com.vishalgaur.shoppingapp.databinding.FragmentSelectPaymentBinding

private const val TAG = "SelectMethodFragment"

class SelectPaymentFragment : Fragment() {

	private lateinit var binding: FragmentSelectPaymentBinding
	private var methodsAdapter = PayByAdapter(getPaymentMethods())

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		binding = FragmentSelectPaymentBinding.inflate(layoutInflater)

		setViews()
		return binding.root
	}

	private fun setViews() {
		binding.payByAppBar.topAppBar.title = getString(R.string.pay_by_title)
		binding.payByAppBar.topAppBar.setNavigationOnClickListener {
			findNavController().navigateUp()
		}
		binding.payByErrorTextView.visibility = View.GONE
		binding.payByPaymentsRecyclerView.adapter = methodsAdapter
		binding.payByNextBtn.text = getString(R.string.pay_by_next_btn_text, "$123.45")
		binding.payByNextBtn.setOnClickListener {
			navigateToOrderSuccess(methodsAdapter.lastCheckedMethod)
		}
	}

	private fun navigateToOrderSuccess(method: String?) {
		if (method != null) {
			Log.d(TAG, "navigate to order Success")
			binding.payByErrorTextView.visibility = View.GONE
		} else {
			Log.d(TAG, "Error: Select a payment method!")
			binding.payByErrorTextView.visibility = View.VISIBLE
		}
	}

	private fun getPaymentMethods(): List<String> {
		return listOf("UPI", "Debit Card", "Cash On Delivery")
	}
}