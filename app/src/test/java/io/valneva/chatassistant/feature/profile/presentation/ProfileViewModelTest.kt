package io.valneva.chatassistant.feature.profile.presentation

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.valneva.chatassistant.R
import io.valneva.chatassistant.core.testing.MainDispatcherRule
import io.valneva.chatassistant.feature.auth.domain.AuthInteractor
import io.valneva.chatassistant.feature.auth.domain.AuthUser
import io.valneva.chatassistant.feature.profile.data.ProfileRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authInteractor = mockk<AuthInteractor>()
    private val repository = mockk<ProfileRepository>()

    @Test
    fun `onSaveNameClick updates state and emits success message`() = runTest {
        val user = MutableStateFlow(
            AuthUser(
                uid = "user-1",
                email = "user@example.com",
                displayName = "Anna",
                photoUrl = null,
                phoneNumber = null,
            ),
        )

        every { authInteractor.getCurrentUser() } returns user.value
        every { authInteractor.observeCurrentUser() } returns user
        every { repository.observeTotalTokens("user-1") } returns MutableStateFlow(12)
        coEvery { repository.updateDisplayName("Kate") } returns Result.success("Kate")

        val viewModel = ProfileViewModel(
            authInteractor = authInteractor,
            profileRepository = repository,
        )

        viewModel.effects.test {
            viewModel.onDisplayNameChanged("Kate")
            viewModel.onSaveNameClick()
            advanceUntilIdle()

            assertThat(awaitItem()).isEqualTo(ProfileUiEffect.ShowSnackbar(R.string.profile_name_saved))
            assertThat(viewModel.uiState.value.displayName).isEqualTo("Kate")
            assertThat(viewModel.uiState.value.displayNameInput).isEqualTo("Kate")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onSignOutClick emits signed out effect`() = runTest {
        val user = MutableStateFlow(
            AuthUser(
                uid = "user-1",
                email = "user@example.com",
                displayName = "Anna",
                photoUrl = null,
                phoneNumber = null,
            ),
        )

        every { authInteractor.getCurrentUser() } returns user.value
        every { authInteractor.observeCurrentUser() } returns user
        every { repository.observeTotalTokens("user-1") } returns MutableStateFlow(0)
        every { repository.signOut() } just runs

        val viewModel = ProfileViewModel(
            authInteractor = authInteractor,
            profileRepository = repository,
        )

        viewModel.effects.test {
            viewModel.onSignOutClick()
            assertThat(awaitItem()).isEqualTo(ProfileUiEffect.SignedOut)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { repository.signOut() }
    }
}
