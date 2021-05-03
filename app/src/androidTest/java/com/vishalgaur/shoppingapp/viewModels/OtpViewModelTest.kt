package com.vishalgaur.shoppingapp.viewModels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vishalgaur.shoppingapp.database.user.UserData
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OtpViewModelTest {
    private lateinit var uData: UserData
    private lateinit var otpViewModel: OtpViewModel

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {

    }
}