package com.novumlogic.credmanager.di

import com.novumlogic.credmanager.data.CredManagerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object BindingModule {

    @Provides
    fun providesRepository(): CredManagerRepository {
        return CredManagerRepository()
    }
}