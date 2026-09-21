package com.pawsmedic.features.reports.domain.usecase

import com.pawsmedic.features.reports.domain.model.Report
import com.pawsmedic.features.reports.domain.repository.ReportRepository

class GetReportUseCase(private val repository: ReportRepository) {
    suspend operator fun invoke(reportId: String): Result<Report> =
        repository.getReport(reportId)
}
