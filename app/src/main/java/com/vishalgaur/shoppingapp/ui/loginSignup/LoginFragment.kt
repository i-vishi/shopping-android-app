package com.vishalgaur.shoppingapp.ui.loginSignup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.vishalgaur.shoppingapp.R
import com.vishalgaur.shoppingapp.databinding.FragmentLoginBinding

class LoginFragment: LoginSignupBaseFragment<FragmentLoginBinding>() {
    override fun setViewBinding(): FragmentLoginBinding {
        return FragmentLoginBinding.inflate(layoutInflater)
    }

    override fun observeView() {
        super.observeView()
    }

    override fun setUpViews() {
        super.setUpViews()
    }
}