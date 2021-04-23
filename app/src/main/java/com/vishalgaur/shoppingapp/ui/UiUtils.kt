package com.vishalgaur.shoppingapp.ui

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.vishalgaur.shoppingapp.ui.home.MainActivity

enum class SignUpViewErrors { NONE, ERR_EMAIL, ERR_MOBILE, ERR_EMAIL_MOBILE, ERR_EMPTY, ERR_NOT_ACC, ERR_PWD12NS }

enum class LoginViewErrors { NONE, ERR_EMPTY, ERR_MOBILE }

enum class OTPStatus { NONE, CORRECT, WRONG }

enum class AddProductViewErrors {NONE, EMPTY, ERR_PRICE_0}

class MyOnFocusChangeListener : View.OnFocusChangeListener {
    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (v != null) {
            val inputManager =
                v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (!hasFocus) {

                inputManager.hideSoftInputFromWindow(v.windowToken, 0)
            } else {
                inputManager.toggleSoftInputFromWindow(v.windowToken, 0, 0)

            }
        }
    }
}

internal fun launchHome(context: Context) {
    val homeIntent = Intent(context, MainActivity::class.java)
    homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(homeIntent)
}