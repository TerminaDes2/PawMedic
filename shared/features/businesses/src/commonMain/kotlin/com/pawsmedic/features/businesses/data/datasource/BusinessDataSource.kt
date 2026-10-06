package com.pawsmedic.features.businesses.data.datasource

import com.pawsmedic.features.businesses.data.dto.BusinessDto

interface BusinessDataSource {
    suspend fun getBusinesses(): List<BusinessDto>
    suspend fun getBusinessById(id: String): BusinessDto?
}
