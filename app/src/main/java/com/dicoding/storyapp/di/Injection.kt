package com.dicoding.storyapp.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.dicoding.storyapp.data.StoryDatabase
import com.dicoding.storyapp.data.UserPreference
import com.dicoding.storyapp.remote.ApiConfig
import com.dicoding.storyapp.remote.ApiService
import com.dicoding.storyapp.repository.StoryRepository

object Injection {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    fun provideRepository(context: Context): StoryRepository {
        val apiService = ApiConfig.getApiService()
        val database = StoryDatabase.getDatabase(context)
        val pref = UserPreference.getInstance(context.dataStore)
        return StoryRepository.getInstance(apiService, database, pref)
    }

    fun provideApiService(): ApiService {
        return ApiConfig.getApiService()
    }
}