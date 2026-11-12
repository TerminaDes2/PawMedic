package com.pawsmedic.features.auth.domain.usecase

import com.pawsmedic.features.auth.domain.model.AuthenticatedSession
import com.pawsmedic.features.auth.domain.model.PawMedicRole
import com.pawsmedic.features.auth.domain.repository.AuthRepository

internal suspend fun AuthRepository.requireCustomerSession(
    session: AuthenticatedSession
): AuthenticatedSession {
    if (session.role != PawMedicRole.USER) {
        signOut()
        throw IllegalArgumentException(
            "Esta aplicación móvil es exclusiva para clientes. Las cuentas de veterinaria o superadmin deben iniciar sesión desde la aplicación de escritorio."
        )
    }
    return session
}
