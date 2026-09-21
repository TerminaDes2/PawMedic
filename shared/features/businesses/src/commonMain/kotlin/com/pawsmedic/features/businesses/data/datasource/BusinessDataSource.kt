package com.pawsmedic.features.businesses.data.datasource

import com.pawsmedic.features.businesses.data.dto.BusinessDto

interface BusinessDataSource {
    suspend fun searchBusinesses(query: String): List<BusinessDto>
}

class EmptyBusinessDataSource : BusinessDataSource {
    override suspend fun searchBusinesses(query: String): List<BusinessDto> = emptyList()
}
