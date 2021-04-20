package com.vishalgaur.shoppingapp.ui.loginSignup

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.vishalgaur.shoppingapp.ui.MyOnFocusChangeListener
import com.vishalgaur.shoppingapp.viewModels.AuthViewModel
import com.vishalgaur.shoppingapp.viewModels.AuthViewModelFactory

abstract class LoginSignupBaseFragment<VBinding : ViewBinding> : Fragment() {

    protected lateinit var viewModel: AuthViewModel

    protected lateinit var binding: VBinding
    protected abstract fun setViewBinding(): VBinding

    protected val focusChangeListener = MyOnFocusChangeListener()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setUpViews()
        observeView()
        return binding.root
    }

    fun launchOtpActivity(from: String, extras: Bundle) {
        val intent = Intent(context, OtpActivity::class.java).putExtra(
            "from",
            from
        ).putExtras(extras)
        startActivity(intent)
    }

    open fun setUpViews() {}

    open fun observeView() {}

    private fun init() {
        binding = setViewBinding()
        if (this.activity != null) {
            val viewModelFactory = AuthViewModelFactory(this.requireActivity().application)
            viewModel =
                ViewModelProvider(this, viewModelFactory).get(AuthViewModel::class.java)
        }
    }

    interface OnClickListener: View.OnClickListener
}