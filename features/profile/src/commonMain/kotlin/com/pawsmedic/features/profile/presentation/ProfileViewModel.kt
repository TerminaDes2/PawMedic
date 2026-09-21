package com.pawsmedic.features.profile.presentation

import com.pawsmedic.core.data.ProfileRepository
import com.pawsmedic.features.profile.domain.ProfileState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: ProfileRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val _state = MutableStateFlow<ProfileState>(ProfileState.Empty)
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    fun load(userId: String) {
        scope.launch {
            _state.value = ProfileState.Loading
            repository.getProfile(userId)
                .onSuccess { _state.value = ProfileState.Loaded(it) }
                .onFailure { _state.value = ProfileState.Error(it.message ?: "Unable to load profile") }
        }
    }
}
