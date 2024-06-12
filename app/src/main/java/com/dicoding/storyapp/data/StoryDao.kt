package com.dicoding.storyapp.data

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface StoryDao {
    @Query("SELECT * FROM story")
    fun getAllStories(): PagingSource<Int, StoryItem>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertStory(story: StoryItem)

    @Query("DELETE FROM story")
    suspend fun deleteAll()

    @Query("SELECT * FROM story WHERE lat IS NOT NULL AND lon IS NOT NULL")
    fun getAllStoriesWithLocation(): LiveData<List<StoryItem>>


}