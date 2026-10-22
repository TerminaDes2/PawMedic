package com.pawsmedic.features.pets.di

import com.pawsmedic.features.pets.data.datasource.InMemoryPetDataSource
import com.pawsmedic.features.pets.data.datasource.PetDataSource
import com.pawsmedic.features.pets.data.repository.DefaultPetRepository
import com.pawsmedic.features.pets.domain.repository.PetRepository
import com.pawsmedic.features.pets.domain.usecase.AddPetUseCase
import com.pawsmedic.features.pets.domain.usecase.DeletePetUseCase
import com.pawsmedic.features.pets.domain.usecase.GetPetByIdUseCase
import com.pawsmedic.features.pets.domain.usecase.GetPetsUseCase
import com.pawsmedic.features.pets.domain.usecase.UpdatePetUseCase
import com.pawsmedic.features.pets.presentation.PetDetailViewModel
import com.pawsmedic.features.pets.presentation.PetFormViewModel
import com.pawsmedic.features.pets.presentation.PetListViewModel
import org.koin.dsl.module

fun petsModule() = module {
    single<PetDataSource> { InMemoryPetDataSource() }
    single<PetRepository> { DefaultPetRepository(get()) }
    factory { GetPetsUseCase(get()) }
    factory { GetPetByIdUseCase(get()) }
    factory { AddPetUseCase(get()) }
    factory { UpdatePetUseCase(get()) }
    factory { DeletePetUseCase(get()) }
    factory { PetListViewModel(get()) }
    factory { (petId: String) -> PetDetailViewModel(get(), get(), petId) }
    factory { (petId: String?) -> PetFormViewModel(get(), get(), get(), petId) }
}
