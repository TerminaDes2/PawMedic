package com.pawsmedic.features.profile.domain

import com.pawsmedic.core.model.UserProfile

sealed interface ProfileState {
    data object Empty : ProfileState
    data object Loading : ProfileState
    data class Loaded(val profile: UserProfile) : ProfileState
    data class Error(val message: String) : ProfileState
}
