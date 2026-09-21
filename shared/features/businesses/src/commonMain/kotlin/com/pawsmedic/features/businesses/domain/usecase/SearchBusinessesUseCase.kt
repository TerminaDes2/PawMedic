package com.pawsmedic.features.businesses.domain.usecase

import com.pawsmedic.features.businesses.domain.model.Business
import com.pawsmedic.features.businesses.domain.repository.BusinessRepository

class SearchBusinessesUseCase(private val repository: BusinessRepository) {
    suspend operator fun invoke(query: String): Result<List<Business>> =
        repository.searchBusinesses(query)
}
