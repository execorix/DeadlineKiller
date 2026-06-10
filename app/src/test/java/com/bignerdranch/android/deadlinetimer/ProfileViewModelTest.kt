package com.bignerdranch.android.deadlinetimer

import android.content.Context
import com.bignerdranch.android.deadlinetimer.data.local.datastore.ProfilePreferences
import com.bignerdranch.android.deadlinetimer.data.repository.DeadlineRepository
import com.bignerdranch.android.deadlinetimer.ui.profile.ProfileViewModel
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import okhttp3.internal.tls.OkHostnameVerifier.verify
import org.junit.Test

import org.junit.Assert.*

class ProfileViewModelTest {
    private val repository = mockk<DeadlineRepository>(relaxed = true)
    private val profilePrefs = mockk<ProfilePreferences>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)

    @Test
    fun `updateAccountData updates state correctly`() = runTest {
        val viewModel = ProfileViewModel(repository, context)

        viewModel.updateAccountData(context, "execorix", "test@gmail.com", "123132123123")


        assert(viewModel.userName.value == "NewName")
        verify { profilePrefs.saveAccountData("NewName", "new@mail.ru") }
    }
}