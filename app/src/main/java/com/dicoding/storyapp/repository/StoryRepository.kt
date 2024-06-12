package com.dicoding.storyapp.repository

import androidx.lifecycle.LiveData
import androidx.paging.*
import com.dicoding.storyapp.data.*
import com.dicoding.storyapp.remote.ApiService
import com.dicoding.storyapp.response.AddNewStoryResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody

class StoryRepository private constructor(
    private val apiService: ApiService,
    private val storyDatabase: StoryDatabase,
    private val userPreference: UserPreference

) {
    fun getAllStories(): LiveData<PagingData<StoryItem>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(pageSize = 5),
            remoteMediator = StoryRemoteMediator(apiService, storyDatabase, userPreference),
            pagingSourceFactory = {
                storyDatabase.storyDao().getAllStories()
            }
        ).liveData
    }

     suspend fun getStoryDetail(token: String, id: String): StoryItem? {
       return try {
            apiService.getStoryDetail("Bearer $token", id).story
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAllStoriesWithLocation(token: String): List<StoryItem> {
        return try {
            apiService.getAllStory("Bearer $token", page = null, size = null, location = 1).listStory
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun uploadStory(token: String, imageMultipart: MultipartBody.Part, description: RequestBody, lat: RequestBody?, lon: RequestBody?): AddNewStoryResponse {
        return apiService.addNewStory("Bearer $token", imageMultipart, description, lat, lon)
    }


    companion object {
        @Volatile
        private var INSTANCE: StoryRepository? = null

        fun getInstance(
            apiService: ApiService,
            storyDatabase: StoryDatabase,
            userPreference: UserPreference
        ): StoryRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = StoryRepository(apiService, storyDatabase,userPreference)
                INSTANCE = instance
                instance
            }
        }
    }

}