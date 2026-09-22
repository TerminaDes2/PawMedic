package com.pawsmedic.features.reports.domain.model

data class Report(
    val id: String,
    val businessId: String,
    val title: String,
    val content: String,
    val createdAt: String
)
