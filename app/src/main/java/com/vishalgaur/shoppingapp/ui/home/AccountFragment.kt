package com.vishalgaur.shoppingapp.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.vishalgaur.shoppingapp.R
import com.vishalgaur.shoppingapp.databinding.FragmentAccountBinding

private const val TAG = "AccountFragment"

class AccountFragment: Fragment() {

	private lateinit var binding: FragmentAccountBinding

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		binding = FragmentAccountBinding.inflate(layoutInflater)

		setViews()
		return binding.root
	}

	private fun setViews() {
		binding.accountTopAppBar.topAppBar.title = getString(R.string.account_fragment_title)
		binding.accountProfileTv.setOnClickListener {
			Log.d(TAG, "Profile Selected")
		}
		binding.accountOrdersTv.setOnClickListener {
			Log.d(TAG, "Orders Selected")
		}
		binding.accountAddressTv.setOnClickListener {
			Log.d(TAG, "Address Selected")
		}
	}
}