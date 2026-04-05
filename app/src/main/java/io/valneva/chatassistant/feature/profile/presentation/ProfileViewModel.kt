package io.valneva.chatassistant.feature.profile.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.valneva.chatassistant.R
import io.valneva.chatassistant.feature.auth.domain.AuthInteractor
import io.valneva.chatassistant.feature.profile.data.ProfileRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authInteractor: AuthInteractor,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val currentUser = authInteractor.observeCurrentUser()

    private val _uiState = MutableStateFlow(
        ProfileUiState(
            displayNameInput = authInteractor.getCurrentUser()?.displayName.orEmpty(),
        ),
    )
    val uiState = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<ProfileUiEffect>()
    val effects = _effects.asSharedFlow()

    init {
        observeUser()
        observeTokens()
    }

    private fun observeUser() {
        viewModelScope.launch {
            currentUser.collect { user ->
                val state = _uiState.value
                val shouldSyncInput = state.displayNameInput.isBlank() || state.displayNameInput == state.displayName

                _uiState.value = state.copy(
                    displayName = user?.displayName.orEmpty(),
                    displayNameInput = if (shouldSyncInput) user?.displayName.orEmpty() else state.displayNameInput,
                    email = user?.email.orEmpty(),
                    phoneNumber = user?.phoneNumber.orEmpty(),
                    photoUrl = user?.photoUrl,
                )
            }
        }
    }

    private fun observeTokens() {
        viewModelScope.launch {
            currentUser
                .map { user -> user?.uid }
                .distinctUntilChanged()
                .flatMapLatest { userId ->
                    if (userId.isNullOrBlank()) {
                        flowOf(0)
                    } else {
                        profileRepository.observeTotalTokens(userId)
                    }
                }
                .collect { totalTokens ->
                    _uiState.value = _uiState.value.copy(totalTokens = totalTokens)
                }
        }
    }

    fun onDisplayNameChanged(value: String) {
        _uiState.value = _uiState.value.copy(displayNameInput = value)
    }

    fun onSaveNameClick() {
        val name = _uiState.value.displayNameInput.trim()
        if (name.isBlank()) {
            emitEffect(ProfileUiEffect.ShowSnackbar(R.string.profile_name_empty))
            return
        }

        if (name == _uiState.value.displayName || _uiState.value.isSavingName) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSavingName = true)

            profileRepository.updateDisplayName(name).fold(
                onSuccess = { updatedName ->
                    _uiState.value = _uiState.value.copy(
                        displayName = updatedName,
                        displayNameInput = updatedName,
                        isSavingName = false,
                    )
                    emitEffect(ProfileUiEffect.ShowSnackbar(R.string.profile_name_saved))
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isSavingName = false)
                    emitEffect(ProfileUiEffect.ShowSnackbar(R.string.profile_update_failed))
                },
            )
        }
    }

    fun onPhotoPicked(uri: Uri?) {
        val localUri = uri ?: return
        if (_uiState.value.isUploadingPhoto) return

        val userId = authInteractor.getCurrentUser()?.uid ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploadingPhoto = true)

            profileRepository.updatePhoto(userId = userId, localUri = localUri).fold(
                onSuccess = { remoteUrl ->
                    _uiState.value = _uiState.value.copy(
                        photoUrl = remoteUrl,
                        isUploadingPhoto = false,
                    )
                    emitEffect(ProfileUiEffect.ShowSnackbar(R.string.profile_photo_updated))
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isUploadingPhoto = false)
                    emitEffect(ProfileUiEffect.ShowSnackbar(R.string.profile_update_failed))
                },
            )
        }
    }

    fun onSignOutClick() {
        if (_uiState.value.isSigningOut) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSigningOut = true)
            profileRepository.signOut()
            _effects.emit(ProfileUiEffect.SignedOut)
        }
    }

    private fun emitEffect(effect: ProfileUiEffect) {
        viewModelScope.launch {
            _effects.emit(effect)
        }
    }
}
