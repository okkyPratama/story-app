package com.dicoding.storyapp

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dicoding.storyapp.data.StoryDatabase
import com.dicoding.storyapp.data.UserPreference
import com.dicoding.storyapp.login.LoginViewModel
import com.dicoding.storyapp.maps.MapsViewModel
import com.dicoding.storyapp.register.RegisterViewModel
import com.dicoding.storyapp.remote.ApiService
import com.dicoding.storyapp.repository.StoryRepository

class ViewModelFactory(
    private val pref: UserPreference,
    private val apiService: ApiService,
    private val context: Context
) : ViewModelProvider.NewInstanceFactory() {


    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(pref) as T
            }
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                RegisterViewModel(pref, apiService) as T
            }
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(pref, apiService) as T
            }
            modelClass.isAssignableFrom(StoryViewModel::class.java) -> {
                val storyDatabase = StoryDatabase.getDatabase(context)
                val storyRepository =
                    StoryRepository.getInstance(apiService, storyDatabase, pref)
                StoryViewModel(storyRepository,pref.getToken()) as T
            }
            modelClass.isAssignableFrom(MapsViewModel::class.java) -> {
                val storyDatabase = StoryDatabase.getDatabase(context)
                val storyRepository =
                    StoryRepository.getInstance(apiService, storyDatabase, pref)
                MapsViewModel(storyRepository,pref) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }
}