package com.pawsmedic.features.pets.presentation.profile

import com.pawsmedic.shared.core.model.UserProfile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class ProfileUiState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ProfileViewModel(
    private val supabase: SupabaseClient,
    val userId: String,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                supabase.postgrest["profiles"]
                    .select { filter { eq("id", userId) } }
                    .decodeSingle<UserProfile>()
            }.onSuccess { profile ->
                _uiState.update { it.copy(profile = profile, isLoading = false) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "No se pudo cargar el perfil"
                    )
                }
            }
        }
    }

    fun updatePhotoUrl(url: String) {
        scope.launch {
            runCatching {
                supabase.postgrest["profiles"].update(
                    buildJsonObject { put("photo_url", url) }
                ) {
                    filter { eq("id", userId) }
                }
            }.onSuccess {
                _uiState.update { current ->
                    current.copy(profile = current.profile?.copy(photo_url = url))
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(errorMessage = error.message ?: "No se pudo guardar la foto")
                }
            }
        }
    }

    fun onError(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }
}
