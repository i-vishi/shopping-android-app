package com.vishalgaur.shoppingapp.ui.loginSignup


import android.view.View
import com.vishalgaur.shoppingapp.OTPStatus
import com.vishalgaur.shoppingapp.databinding.FragmentOtpBinding

class OtpFragment : LoginSignupBaseFragment<FragmentOtpBinding>() {
    override fun setViewBinding(): FragmentOtpBinding {
        return FragmentOtpBinding.inflate(layoutInflater)
    }

    override fun setUpViews() {
        super.setUpViews()
        binding.otpVerifyError.visibility = View.GONE

        binding.otpVerifyBtn.setOnClickListener {
            onVerify()
        }
    }

    override fun observeView() {
        super.observeView()
        viewModel.otpStatus.observe(viewLifecycleOwner) {
            when (it) {
                OTPStatus.WRONG -> binding.otpVerifyError.visibility = View.VISIBLE
                else -> binding.otpVerifyError.visibility = View.GONE
            }
        }
    }

    private fun onVerify() {
        val otp = binding.otpOtpEditText.text.toString()
        viewModel.verifyOTP(otp)
    }
}