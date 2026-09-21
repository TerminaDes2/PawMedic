package com.pawsmedic.features.businesses.data.repository

import com.pawsmedic.features.businesses.data.datasource.BusinessDataSource
import com.pawsmedic.features.businesses.domain.model.Business
import com.pawsmedic.features.businesses.domain.repository.BusinessRepository

class DefaultBusinessRepository(
    private val dataSource: BusinessDataSource
) : BusinessRepository {
    override suspend fun searchBusinesses(query: String): Result<List<Business>> = try {
        Result.success(dataSource.searchBusinesses(query).map { it.toDomain() })
    } catch (error: Throwable) {
        Result.failure(error)
    }
}
