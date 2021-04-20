package com.vishalgaur.shoppingapp.ui.loginSignup

import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.vishalgaur.shoppingapp.EMAIL_ERROR_TEXT
import com.vishalgaur.shoppingapp.MOB_ERROR_TEXT
import com.vishalgaur.shoppingapp.R
import com.vishalgaur.shoppingapp.ViewErrors
import com.vishalgaur.shoppingapp.databinding.FragmentSignupBinding
import com.vishalgaur.shoppingapp.network.SignUpErrors

class SignupFragment : LoginSignupBaseFragment<FragmentSignupBinding>() {

    override fun setViewBinding(): FragmentSignupBinding {
        return FragmentSignupBinding.inflate(layoutInflater)
    }

    override fun observeView() {
        super.observeView()
        viewModel.errorStatus.observe(viewLifecycleOwner) { err ->
            modifyErrors(err)
        }
    }

    override fun setUpViews() {
        super.setUpViews()
        binding.signupErrorTextView.visibility = View.GONE

        binding.signupSignupBtn.setOnClickListener {
            onSignUp()
            if (viewModel.errorStatus.value == ViewErrors.NONE) {
                viewModel.signErrorStatus.observe(viewLifecycleOwner) {
                    if (it == SignUpErrors.NONE) {
                        val bundle = bundleOf("uData" to viewModel.userData.value)
                        launchOtpActivity(getString(R.string.signup_fragment_label), bundle)
                    }
                }
            }
        }

        setUpClickableLoginText()
    }

    private fun setUpClickableLoginText() {
        val loginText = getString(R.string.signup_login_text)
        val ss = SpannableString(loginText)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                findNavController().navigate(R.id.action_signup_to_login)
            }
        }

        ss.setSpan(clickableSpan, 25, 31, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.signupLoginTextView.apply {
            text = ss
            movementMethod = LinkMovementMethod.getInstance()
        }
    }

    private fun onSignUp() {
        val name = binding.signupNameEditText.text.toString()
        val mobile = binding.signupMobileEditText.text.toString()
        val email = binding.signupEmailEditText.text.toString()
        val password1 = binding.signupPasswordEditText.text.toString()
        val password2 = binding.signupCnfPasswordEditText.text.toString()
        val isAccepted = binding.signupPolicySwitch.isChecked

        viewModel.signUpSubmitData(name, mobile, email, password1, password2, isAccepted)
    }

    private fun modifyErrors(err: ViewErrors) {
        when (err) {
            ViewErrors.NONE -> setEditTextsError()
            ViewErrors.ERR_EMAIL -> setEditTextsError(emailError = EMAIL_ERROR_TEXT)
            ViewErrors.ERR_MOBILE -> setEditTextsError(mobError = MOB_ERROR_TEXT)
            ViewErrors.ERR_EMAIL_MOBILE -> setEditTextsError(EMAIL_ERROR_TEXT, MOB_ERROR_TEXT)
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