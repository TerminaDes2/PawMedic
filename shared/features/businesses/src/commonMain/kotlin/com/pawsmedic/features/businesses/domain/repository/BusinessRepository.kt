package com.pawsmedic.features.businesses.domain.repository

import com.pawsmedic.features.businesses.domain.model.Business

interface BusinessRepository {
    suspend fun getBusinesses(): Result<List<Business>>
    suspend fun getBusinessById(id: String): Result<Business?>
    suspend fun searchBusinesses(query: String): Result<List<Business>>
}
