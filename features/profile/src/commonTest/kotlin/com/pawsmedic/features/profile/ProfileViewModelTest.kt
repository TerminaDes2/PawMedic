package com.pawsmedic.features.profile

import com.pawsmedic.core.data.InMemoryProfileRepository
import com.pawsmedic.features.profile.domain.ProfileState
import com.pawsmedic.features.profile.presentation.ProfileViewModel
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertIs

class ProfileViewModelTest {
    @Test
    fun loadPublishesProfileAndRole() = runTest {
        val viewModel = ProfileViewModel(InMemoryProfileRepository(), backgroundScope)
        viewModel.load("owner@example.test")
        advanceUntilIdle()
        val loaded = assertIs<ProfileState.Loaded>(viewModel.state.value)
        kotlin.test.assertEquals("owner@example.test", loaded.profile.email)
    }
}
