package com.vishalgaur.shoppingapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.vishalgaur.shoppingapp.databinding.FragmentFavoritesBinding
import com.vishalgaur.shoppingapp.viewModels.HomeViewModel

class FavoritesFragment: Fragment() {
	private lateinit var binding: FragmentFavoritesBinding
	private val viewModel: HomeViewModel by activityViewModels()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		binding = FragmentFavoritesBinding.inflate(layoutInflater)
		setViews()
		setObservers()
		return binding.root
	}

	private fun setViews() {
		binding.favTopAppBar.topAppBar.title = "Favorite Products"
		binding.favTopAppBar.topAppBar.setNavigationOnClickListener {
			findNavController().navigateUp()
		}
	}

	private fun setObservers(){}
}