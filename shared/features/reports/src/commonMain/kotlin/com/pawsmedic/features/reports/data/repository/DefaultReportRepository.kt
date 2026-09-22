package com.pawsmedic.features.reports.data.repository

import com.pawsmedic.features.reports.data.datasource.ReportDataSource
import com.pawsmedic.features.reports.domain.model.Report
import com.pawsmedic.features.reports.domain.repository.ReportRepository

class DefaultReportRepository(
    private val dataSource: ReportDataSource
) : ReportRepository {
    override suspend fun getReport(reportId: String): Result<Report> = try {
        val report = dataSource.getReport(reportId)
        if (report == null) {
            Result.failure(NoSuchElementException("Report not found: $reportId"))
        } else {
            Result.success(report.toDomain())
        }
    } catch (error: Throwable) {
        Result.failure(error)
    }
}
