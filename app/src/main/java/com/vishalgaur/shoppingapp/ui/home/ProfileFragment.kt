package com.vishalgaur.shoppingapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.vishalgaur.shoppingapp.R
import com.vishalgaur.shoppingapp.databinding.FragmentProfileBinding
import com.vishalgaur.shoppingapp.viewModels.HomeViewModel

class ProfileFragment : Fragment() {

	private lateinit var binding: FragmentProfileBinding
	private val viewModel: HomeViewModel by activityViewModels()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		binding = FragmentProfileBinding.inflate(layoutInflater)
		binding.profileTopAppBar.topAppBar.title = getString(R.string.account_profile_label)
		binding.profileTopAppBar.topAppBar.setNavigationOnClickListener {
			findNavController().navigateUp()
		}
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		viewModel.getUserData()
		setViews()
	}

	private fun setViews() {
		viewModel.userData.observe(viewLifecycleOwner) {
			if (it != null) {
				binding.profileNameTv.text = it.name
				binding.profileEmailTv.text = it.email
				binding.profileMobileTv.text = it.mobile
			}
		}
	}
}