package com.pawsmedic.features.businesses.domain.repository

import com.pawsmedic.features.businesses.domain.model.Business

interface BusinessRepository {
    suspend fun searchBusinesses(query: String): Result<List<Business>>
}
