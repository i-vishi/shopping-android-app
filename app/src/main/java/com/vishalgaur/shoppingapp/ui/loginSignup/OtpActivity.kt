package com.vishalgaur.shoppingapp.ui.loginSignup

import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vishalgaur.shoppingapp.OTPStatus
import com.vishalgaur.shoppingapp.R
import com.vishalgaur.shoppingapp.database.UserData
import com.vishalgaur.shoppingapp.databinding.ActivityOtpBinding
import com.vishalgaur.shoppingapp.network.LogInErrors
import com.vishalgaur.shoppingapp.ui.launchHome
import com.vishalgaur.shoppingapp.viewModels.OtpViewModel
import java.lang.IllegalArgumentException

class OtpActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOtpBinding

    private lateinit var viewModel: OtpViewModel

    private lateinit var fromWhere: String

    class OtpViewModelFactory(
        private val application: Application, private val uData: UserData
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(OtpViewModel::class.java)) {
                return OtpViewModel(application, uData) as T
            }
            throw IllegalArgumentException("Unknown ViewModel Class")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)
        val uData: UserData? = intent.getParcelableExtra("uData")
        fromWhere = intent.getStringExtra("from").toString()
        if (uData != null) {
            val viewModelFactory = OtpViewModelFactory(application, uData)
            viewModel =
                ViewModelProvider(this, viewModelFactory).get(OtpViewModel::class.java)

            viewModel.verifyPhoneOTPStart(uData.mobile, this)
        }
        setViews()

        setObservers()
        setContentView(binding.root)
    }

    private fun setObservers() {
        viewModel.otpStatus.observe(this) {
            when (it) {
                OTPStatus.WRONG -> binding.otpVerifyError.visibility = View.VISIBLE
                else -> binding.otpVerifyError.visibility = View.GONE
            }
        }

        viewModel.authRepository.isLoggedIn.observe(this) {
            if (it == true) {
                if (fromWhere == getString(R.string.signup_fragment_label)) {
                    viewModel.signUp()
                }
                else {
                    viewModel.login()
                }
                launchHome(this)
                finish()
            }
        }

//        viewModel.loginStatus.observe(this) {
//            if (it == LogInErrors.NONE) {
//                if (fromWhere == getString(R.string.signup_fragment_label)) {
//                    viewModel.signUp()
//                }
//                launchHome(this)
//                finish()
//            }
//        }
    }

    private fun setViews() {
        binding.otpVerifyError.visibility = View.GONE

        binding.otpVerifyBtn.setOnClickListener {
            onVerify()
        }
    }

    private fun onVerify() {
        val otp = binding.otpOtpEditText.text.toString()
        viewModel.verifyOTP(otp)

    }
}