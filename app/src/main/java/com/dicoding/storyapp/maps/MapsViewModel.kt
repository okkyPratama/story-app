package com.dicoding.storyapp.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.storyapp.data.StoryItem
import com.dicoding.storyapp.data.UserPreference
import com.dicoding.storyapp.repository.StoryRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MapsViewModel(
    private val storyRepository: StoryRepository,
    private val userPreference: UserPreference

) : ViewModel() {

    private val _storiesWithLocation = MutableLiveData<List<StoryItem>>()
    val storiesWithLocation: LiveData<List<StoryItem>> get() = _storiesWithLocation
    val isLoading = MutableLiveData<Boolean>()


    init {
        getAllStoriesWithLocation()

    }

    private fun getAllStoriesWithLocation() {
        viewModelScope.launch {
            isLoading.value = true
            val token = userPreference.getToken().first()
            val stories = storyRepository.getAllStoriesWithLocation(token)
            _storiesWithLocation.value = stories
            isLoading.value = false

        }
    }
}