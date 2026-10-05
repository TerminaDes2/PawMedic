package com.pawsmedic.features.businesses.data.repository

import com.pawsmedic.features.businesses.data.datasource.BusinessDataSource
import com.pawsmedic.features.businesses.domain.model.Business
import com.pawsmedic.features.businesses.domain.repository.BusinessRepository

class DefaultBusinessRepository(
    private val dataSource: BusinessDataSource
) : BusinessRepository {
    override suspend fun getBusinesses(): Result<List<Business>> = try {
        Result.success(dataSource.getBusinesses().map { it.toDomain() })
    } catch (error: Throwable) {
        Result.failure(error)
    }

    override suspend fun getBusinessById(id: String): Result<Business?> = try {
        Result.success(dataSource.getBusinessById(id)?.toDomain())
    } catch (error: Throwable) {
        Result.failure(error)
    }

    override suspend fun searchBusinesses(query: String): Result<List<Business>> = try {
        val all = dataSource.getBusinesses().map { it.toDomain() }
        val filtered = if (query.isBlank()) all else all.filter {
            it.name.contains(query, ignoreCase = true) ||
            (it.address?.contains(query, ignoreCase = true) == true) ||
            it.specialties.any { spec -> spec.contains(query, ignoreCase = true) }
        }
        Result.success(filtered)
    } catch (error: Throwable) {
        Result.failure(error)
    }
}
