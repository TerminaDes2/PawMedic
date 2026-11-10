package com.pawsmedic.features.medical_records.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AuditLogEntry(
    val id: String = "",
    val actorId: String = "",
    val actorName: String = "",
    val actorRole: String = "",
    val timestamp: String = "",
    val entityType: String = "",
    val entityId: String = "",
    val action: String = "", // INSERT, UPDATE, DELETE
    val details: String = ""
)
