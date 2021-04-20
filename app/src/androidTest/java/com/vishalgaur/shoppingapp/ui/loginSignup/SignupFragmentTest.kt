package com.vishalgaur.shoppingapp.ui.loginSignup

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vishalgaur.shoppingapp.R
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignupFragmentTest {
    private lateinit var signUpScenario: FragmentScenario<SignupFragment>

    @Before
    fun setUp() {
        signUpScenario = launchFragmentInContainer(themeResId = R.style.Theme_ShoppingApp)
    }

    @Test
    fun useAppContext() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.vishalgaur.shoppingapp", context.packageName)
    }

    private fun insertInNameEditText(text: String) =
        onView(withId(R.id.signup_name_edit_text)).perform(scrollTo(), clearText(), typeText(text))
}