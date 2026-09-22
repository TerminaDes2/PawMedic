package com.pawsmedic.features.reports.data.dto

import com.pawsmedic.features.reports.domain.model.Report
import kotlinx.serialization.Serializable

@Serializable
data class ReportDto(
    val id: String,
    val businessId: String,
    val title: String,
    val content: String,
    val createdAt: String
) {
    fun toDomain() = Report(id, businessId, title, content, createdAt)
}
