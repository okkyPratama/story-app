package com.dicoding.storyapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dicoding.storyapp.data.StoryItem
import com.dicoding.storyapp.repository.StoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class StoryViewModel(
    private val storyRepository: StoryRepository,
    private val token: Flow<String>,
) : ViewModel() {


    private var _stories = storyRepository.getAllStories().cachedIn(viewModelScope)
    val stories: LiveData<PagingData<StoryItem>> get() = _stories

    private val _storyDetail = MutableLiveData<StoryItem?>()
    val storyDetail: LiveData<StoryItem?> get() = _storyDetail

    enum class LoadingStatus { LOADING, SUCCESS, ERROR }
    val loadingStatus = MutableLiveData<LoadingStatus>()
    val uploadStatus = MutableLiveData<LoadingStatus>()


    fun getStoryDetail(storyId: String) {
        viewModelScope.launch {
            loadingStatus.value = LoadingStatus.LOADING
            try {
                val tokenValue = token.first()
                val story = storyRepository.getStoryDetail(tokenValue, storyId)
                _storyDetail.postValue(story)
                loadingStatus.value = LoadingStatus.SUCCESS
            } catch (e: Exception) {
                loadingStatus.value = LoadingStatus.ERROR
            }
        }
    }

    fun uploadStory(token: String, imageFile: File, description: String, lat: Double?, lon: Double?) {
        viewModelScope.launch {
            try {
                uploadStatus.value = LoadingStatus.LOADING

                val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                    "photo",
                    imageFile.name,
                    requestImageFile
                )
                val descriptionRequestBody = description.toRequestBody("text/plain".toMediaType())
                val latRequestBody = lat?.toString()?.toRequestBody("text/plain".toMediaType())
                val lonRequestBody = lon?.toString()?.toRequestBody("text/plain".toMediaType())

                val response = storyRepository.uploadStory(token, imageMultipart, descriptionRequestBody, latRequestBody, lonRequestBody)
                if (!response.error) {
                    uploadStatus.value = LoadingStatus.SUCCESS
                    refreshStories()
                } else {
                    uploadStatus.value = LoadingStatus.ERROR
                }
            } catch (e: Exception) {
                uploadStatus.value = LoadingStatus.ERROR
            }
        }

    }


    fun refreshStories() {
        _stories = storyRepository.getAllStories().cachedIn(viewModelScope)
    }


}