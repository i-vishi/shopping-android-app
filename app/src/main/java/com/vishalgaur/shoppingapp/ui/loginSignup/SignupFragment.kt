package com.vishalgaur.shoppingapp.ui.loginSignup

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.vishalgaur.shoppingapp.EMAIL_ERROR
import com.vishalgaur.shoppingapp.MOB_ERROR
import com.vishalgaur.shoppingapp.R
import com.vishalgaur.shoppingapp.ViewErrors
import com.vishalgaur.shoppingapp.databinding.FragmentSignupBinding
import com.vishalgaur.shoppingapp.viewModels.AuthViewModel
import com.vishalgaur.shoppingapp.viewModels.AuthViewModelFactory

class SignupFragment : Fragment() {

    private lateinit var binding: FragmentSignupBinding

    private lateinit var authViewModel: AuthViewModel

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSignupBinding.inflate(inflater, container, false)

        if (this.activity != null) {
            val viewModelFactory = AuthViewModelFactory(this.requireActivity().application)
            authViewModel = ViewModelProvider(this, viewModelFactory).get(AuthViewModel::class.java)
        }

        setViews()

        setObservers()

        return binding.root
    }

    private fun setObservers() {
        authViewModel.errorStatus.observe(viewLifecycleOwner, { err ->
            modifyErrors(err)
        })
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun setViews() {
        binding.signupErrorTextView.visibility = View.GONE

        binding.signupSignupBtn.setOnClickListener {
            onSignUp()
            if (authViewModel.errorStatus.value == ViewErrors.NONE)
                findNavController().navigate(R.id.action_signup_to_otp)
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun onSignUp() {
        val name = binding.signupNameEditText.text.toString()
        val mobile = binding.signupMobileEditText.text.toString()
        val email = binding.signupEmailEditText.text.toString()
        val password1 = binding.signupPasswordEditText.text.toString()
        val password2 = binding.signupCnfPasswordEditText.text.toString()
        val isAccepted = binding.signupPolicySwitch.isChecked

        authViewModel.submitData(name, mobile, email, password1, password2, isAccepted)
    }

    private fun modifyErrors(err: ViewErrors) {
        when (err) {
            ViewErrors.NONE -> setEditTextsError()
            ViewErrors.ERR_EMAIL -> setEditTextsError(emailError = EMAIL_ERROR)
            ViewErrors.ERR_MOBILE -> setEditTextsError(mobError = MOB_ERROR)
            ViewErrors.ERR_EMAIL_MOBILE -> setEditTextsError(EMAIL_ERROR, MOB_ERROR)
            ViewErrors.ERR_EMPTY -> setErrorText("Fill all details.")
            ViewErrors.ERR_NOT_ACC -> setErrorText("Accept the Terms.")
            ViewErrors.ERR_PWD12NS -> setErrorText("Both passwords are not same!")
        }
    }

    private fun setErrorText(errText: String?) {
        binding.signupErrorTextView.visibility = View.VISIBLE
        if (errText != null) {
            binding.signupErrorTextView.text = errText
        }
    }

    private fun setEditTextsError(emailError: String? = null, mobError: String? = null) {
        binding.signupEmailEditText.error = emailError
        binding.signupMobileEditText.error = mobError
        binding.signupErrorTextView.visibility = View.GONE
    }
}