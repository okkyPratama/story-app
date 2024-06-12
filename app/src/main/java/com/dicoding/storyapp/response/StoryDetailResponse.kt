package com.dicoding.storyapp.response

import com.dicoding.storyapp.data.StoryItem
import com.google.gson.annotations.SerializedName

data class StoryDetailResponse(
    @field:SerializedName("story")
    val story: StoryItem,

    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String
)
