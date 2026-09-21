package com.pawsmedic.features.reports.domain.repository

import com.pawsmedic.features.reports.domain.model.Report

interface ReportRepository {
    suspend fun getReport(reportId: String): Result<Report>
}
