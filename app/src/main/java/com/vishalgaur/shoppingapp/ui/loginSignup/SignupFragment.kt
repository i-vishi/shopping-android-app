package com.vishalgaur.shoppingapp.ui.loginSignup

import android.content.Intent
import android.os.Build
import android.os.Parcelable
import android.view.View
import androidx.annotation.RequiresApi
import com.vishalgaur.shoppingapp.EMAIL_ERROR
import com.vishalgaur.shoppingapp.MOB_ERROR
import com.vishalgaur.shoppingapp.ViewErrors
import com.vishalgaur.shoppingapp.databinding.FragmentSignupBinding

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

    @RequiresApi(Build.VERSION_CODES.P)
    override fun setUpViews() {
        super.setUpViews()
        binding.signupErrorTextView.visibility = View.GONE

        binding.signupSignupBtn.setOnClickListener {
            onSignUp()
            if (viewModel.errorStatus.value == ViewErrors.NONE)
                launchOtpActivity()
        }
    }


    private fun launchOtpActivity() {
        val intent = Intent(context, OtpActivity::class.java).putExtra(
            "uData",
            viewModel.userData.value
        )
        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun onSignUp() {
        val name = binding.signupNameEditText.text.toString()
        val mobile = binding.signupMobileEditText.text.toString()
        val email = binding.signupEmailEditText.text.toString()
        val password1 = binding.signupPasswordEditText.text.toString()
        val password2 = binding.signupCnfPasswordEditText.text.toString()
        val isAccepted = binding.signupPolicySwitch.isChecked

        activity?.let {
            viewModel.submitData(
                name, mobile, email, password1, password2, isAccepted,
                it
            )
        }
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