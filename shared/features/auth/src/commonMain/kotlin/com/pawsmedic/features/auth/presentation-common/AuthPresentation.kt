package com.pawsmedic.features.auth.presentation_common

import com.pawsmedic.features.auth.presentation.AuthUiState

fun AuthUiState.isBusy() = this is AuthUiState.Loading
