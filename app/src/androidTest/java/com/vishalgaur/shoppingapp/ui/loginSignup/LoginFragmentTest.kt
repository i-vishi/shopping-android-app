package com.vishalgaur.shoppingapp.ui.loginSignup

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import androidx.test.platform.app.InstrumentationRegistry
import com.vishalgaur.shoppingapp.MOB_ERROR_TEXT
import com.vishalgaur.shoppingapp.R
import com.vishalgaur.shoppingapp.clickClickableSpan
import com.vishalgaur.shoppingapp.data.ShoppingAppSessionManager
import org.hamcrest.Matchers.`is`
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginFragmentTest {
	private lateinit var loginScenario: FragmentScenario<LoginFragment>
	private lateinit var navController: NavController
	private lateinit var sessionManager: ShoppingAppSessionManager

	@Before
	fun setUp() {
		sessionManager = ShoppingAppSessionManager(ApplicationProvider.getApplicationContext())
		sessionManager.logoutFromSession()
		loginScenario = launchFragmentInContainer(themeResId = R.style.Theme_ShoppingApp)
		navController = TestNavHostController(ApplicationProvider.getApplicationContext())

		runOnUiThread {
			navController.setGraph(R.navigation.signup_nav_graph)
			loginScenario.onFragment {
				Navigation.setViewNavController(it.requireView(), navController)
			}
		}
	}

	@Test
	fun useAppContext() {
		val context = InstrumentationRegistry.getInstrumentation().targetContext
		Assert.assertEquals("com.vishalgaur.shoppingapp", context.packageName)
	}

	@Test
	fun userCanEnterMobile() {
		insertInMobileEditText("8976527465")
	}

	@Test
	fun userCanEnterPassword() {
		insertInPwdEditText("dh239048fy")
	}

	@Test
	fun userCanClickRemSwitch() {
		clickRememberSwitch()
	}

	@Test
	fun userCanClickSignUpText() {
		clickSignUpText()
	}

	@Test
	fun userCanClickForgotTextView() {
		clickForgotTextView()
	}

	@Test
	fun userCanClickLoginButton() {
		clickLoginButton()
	}

	@Test
	fun onSignUpClick_navigateToSignUpFragment() {
		clickSignUpText()
		assertThat(navController.currentDestination?.id, `is`(R.id.SignupFragment))
	}

	@Test
	fun onLogin_emptyForm_showsError() {
		clickLoginButton()

		onView(withId(R.id.login_error_text_view)).check(matches(isDisplayed()))
	}

	@Test
	fun onLogin_invalidMobile_showsError() {
		insertInMobileEditText("  467856 ")
		insertInPwdEditText("fd3g24")
		clickLoginButton()

		onView(withId(R.id.login_mobile_edit_text)).check(matches(hasErrorText(`is`(MOB_ERROR_TEXT))))
	}

	@Test
	fun onLogin_validData_showsNoError() {
		Intents.init()

		insertInMobileEditText("9966339966")
		insertInPwdEditText("1234")
		clickLoginButton()

		intended(hasComponent(OtpActivity::class.java.name))
	}

	private fun insertInMobileEditText(phone: String) =
		onView(withId(R.id.login_mobile_edit_text)).perform(
			scrollTo(),
			clearText(),
			typeText(phone)
		)

	private fun insertInPwdEditText(pwd: String) =
		onView(withId(R.id.login_password_edit_text)).perform(
			scrollTo(),
			clearText(),
			typeText(pwd)
		)

	private fun clickRememberSwitch() =
		onView(withId(R.id.login_rem_switch))
			.perform(scrollTo(), click())

	private fun clickForgotTextView() =
		onView(withId(R.id.login_forgot_tv))
			.perform(scrollTo(), click())

	private fun clickLoginButton() =
		onView(withId(R.id.login_login_btn))
			.perform(scrollTo(), click())

	private fun clickSignUpText() =
		onView(withId(R.id.login_signup_text_view)).perform(
			scrollTo(),
			clickClickableSpan("Sign Up")
		)
}