package com.pawsmedic.features.reports.data.datasource

import com.pawsmedic.features.reports.data.dto.ReportDto

interface ReportDataSource {
    suspend fun getReport(reportId: String): ReportDto?
}

class EmptyReportDataSource : ReportDataSource {
    override suspend fun getReport(reportId: String): ReportDto? = null
}
