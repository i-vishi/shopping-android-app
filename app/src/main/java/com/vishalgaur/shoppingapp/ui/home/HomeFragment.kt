package com.vishalgaur.shoppingapp.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vishalgaur.shoppingapp.R
import com.vishalgaur.shoppingapp.databinding.FragmentHomeBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(layoutInflater)

		setViews()

        return binding.root
    }

	private fun setViews() {
		binding.homeFabAddProduct.setOnClickListener {
			showDialog()
		}
	}


	private fun showDialog() {
        val categoryItems = arrayOf("Shoes")
        val checkedItem = 0
        context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle(getString(R.string.pro_cat_dialog_title))
                .setNegativeButton(getString(R.string.pro_cat_dialog_cancel_btn)) { dialog, which ->
					dialog.cancel()
                }
                .setPositiveButton(getString(R.string.pro_cat_dialog_ok_btn)) { dialog, which ->
					dialog.cancel()

                }
                .setSingleChoiceItems(categoryItems, checkedItem) { dialog, which ->

                }.show()
        }
    }
}