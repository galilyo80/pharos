package com.pharos.evlink.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module. Most classes use constructor injection (@Inject + @Singleton),
 * so this stays light. Add @Provides here only for things Hilt can't build
 * itself (e.g. Room database, Retrofit) in later milestones.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule
