package com.vishalgaur.shoppingapp.ui.loginSignup

import android.app.Application
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vishalgaur.shoppingapp.OTPStatus
import com.vishalgaur.shoppingapp.database.UserData
import com.vishalgaur.shoppingapp.databinding.ActivityOtpBinding
import com.vishalgaur.shoppingapp.viewModels.OtpViewModel
import java.lang.IllegalArgumentException

class OtpActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOtpBinding

    private lateinit var viewModel: OtpViewModel

    class OtpViewModelFactory(
        private val application: Application, private val uData: UserData?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(OtpViewModel::class.java)) {
                return OtpViewModel(application, uData) as T
            }
            throw IllegalArgumentException("Unknown ViewModel Class")
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)

        val uData: UserData? = intent.getParcelableExtra("uData")

        val viewModelFactory = OtpViewModelFactory(application, uData)
        viewModel =
            ViewModelProvider(this, viewModelFactory).get(OtpViewModel::class.java)


        if (uData != null) {
            viewModel.authRepository.verifyPhoneOTPStart(uData.mobile, this)
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
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun setViews() {
        binding.otpVerifyError.visibility = View.GONE

        binding.otpVerifyBtn.setOnClickListener {
            onVerify()
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun onVerify() {
        val otp = binding.otpOtpEditText.text.toString()
        viewModel.verifyOTP(otp)

    }
}