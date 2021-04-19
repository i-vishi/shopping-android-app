package com.vishalgaur.shoppingapp.ui.loginSignup

import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.vishalgaur.shoppingapp.LoginViewErrors
import com.vishalgaur.shoppingapp.MOB_ERROR_TEXT
import com.vishalgaur.shoppingapp.R
import com.vishalgaur.shoppingapp.databinding.FragmentLoginBinding
import com.vishalgaur.shoppingapp.network.LogInErrors

class LoginFragment : LoginSignupBaseFragment<FragmentLoginBinding>() {
    override fun setViewBinding(): FragmentLoginBinding {
        return FragmentLoginBinding.inflate(layoutInflater)
    }

    override fun observeView() {
        super.observeView()

        viewModel.errorStatusLoginFragment.observe(viewLifecycleOwner) { err ->
            modifyErrors(err)
        }

        viewModel.loginErrorStatus.observe(viewLifecycleOwner) { err ->
            when (err) {
                LogInErrors.LERR -> setErrorText(getString(R.string.login_error_text))
                else -> binding.loginErrorTextView.visibility = View.GONE
            }
        }
    }

    override fun setUpViews() {
        super.setUpViews()

        binding.loginErrorTextView.visibility = View.GONE

        binding.loginLoginBtn.setOnClickListener {
            Log.d("TAGTAGTAG", "check  on click")

            onLogin()
            Log.d(
                "TAGTAGTAG",
                "check outside if, ${viewModel.errorStatusLoginFragment.value}, ${viewModel.loginErrorStatus.value}"
            )

            if (viewModel.errorStatusLoginFragment.value == LoginViewErrors.NONE) {
                viewModel.loginErrorStatus.observe(viewLifecycleOwner) {
                    if (it == LogInErrors.NONE) {
                        val bundle = bundleOf(
                            "uData" to viewModel.userData.value
                        )
                        Log.d("TAGTAGTAG", "check inside if")
                        launchOtpActivity(getString(R.string.login_fragment_label), bundle)
                    }
                }
            }
        }

        setUpClickableSignUpText()
    }

    private fun modifyErrors(err: LoginViewErrors) {
        when (err) {
            LoginViewErrors.NONE -> setEditTextErrors()
            LoginViewErrors.ERR_EMPTY -> setErrorText("Fill all details")
            LoginViewErrors.ERR_MOBILE -> setEditTextErrors(MOB_ERROR_TEXT)
        }
    }

    private fun setErrorText(errText: String?) {
        binding.loginErrorTextView.visibility = View.VISIBLE
        if (errText != null) {
            binding.loginErrorTextView.text = errText
        }
    }

    private fun setEditTextErrors(mobError: String? = null) {
        binding.loginErrorTextView.visibility = View.GONE
        binding.loginMobileEditText.error = mobError
    }

    private fun setUpClickableSignUpText() {
        val signUpText = getString(R.string.login_signup_text)
        val ss = SpannableString(signUpText)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                findNavController().navigateUp()
            }
        }

        ss.setSpan(clickableSpan, 10, 17, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.loginSignupTextView.apply {
            text = ss
            movementMethod = LinkMovementMethod.getInstance()
        }
    }

    private fun onLogin() {
        val mob = binding.loginMobileEditText.text.toString()
        val pwd = binding.loginPasswordEditText.text.toString()
        val isRemOn = binding.loginRemSwitch.isChecked

        viewModel.loginSubmitData(mob, pwd, isRemOn)
    }
}